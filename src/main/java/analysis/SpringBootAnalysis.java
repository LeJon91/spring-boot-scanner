package analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.http.HttpMethod;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import API.APIDescription;
import API.Parameter;
import API.RESTfulHttpOperation;

public class SpringBootAnalysis {

	private final String REST_CONTROLLER_ANNOTATION = "@RestController";
	private final String REST_METHOD_ANNOTATION = "@RequestMapping";

	public APIDescription getServiceInterface(String localRepoDir) {

		ArrayList<String> javaFilePath = new ArrayList<String>();
		File localRepo = new File(localRepoDir);
		getJavaFilePathFromDirectory(localRepo, javaFilePath);

		CompilationUnit cu = null;
		boolean isRestController = false;
		boolean isRestMethod = false;
		MethodDeclaration currentMethod = null;
		APIDescription apiDescription = new APIDescription();
		RESTfulHttpOperation operation = null;
		NormalAnnotationExpr normalAnnotation = null;
		String uri = "";
		HttpMethod[] methods = {HttpMethod.GET,HttpMethod.PUT,HttpMethod.POST,HttpMethod.DELETE};
		String[] statusCodes = {};
		//TODO HttpMethod method, String[] statusCodes, String[] acceptableMimeTypes, String[] producableMimeTypes, List<Parameter> parameter
		try {
			for (String path : javaFilePath) {
				cu = JavaParser.parse(new FileInputStream(path));

				// look at every class in the file and check if it is a
				// RestController
				for (TypeDeclaration classNode : cu.getTypes()) {
					if (classNode instanceof ClassOrInterfaceDeclaration) {
						for (AnnotationExpr annotation : classNode.getAnnotations()) {
							if (annotation.toString().equals(REST_CONTROLLER_ANNOTATION)) {
								isRestController = true;
								break;
							}
						}

						if (isRestController) {
							isRestController = false;
							// look at every method and check if it is a
							// Request Mapping
							for (BodyDeclaration classElement : classNode.getMembers()) {
								if (classElement instanceof MethodDeclaration) {
									currentMethod = (MethodDeclaration) classElement;
									annotation:
									for (AnnotationExpr annotation : currentMethod.getAnnotations()) {
										if (annotation.toString().contains(REST_METHOD_ANNOTATION)) {
											if(annotation instanceof NormalAnnotationExpr) {
												normalAnnotation = (NormalAnnotationExpr)annotation;
												for(MemberValuePair memberValuePair:normalAnnotation.getPairs()) {
													if(memberValuePair.getName().equals("path") || memberValuePair.getName().equals("value")) {
														uri = ((StringLiteralExpr)memberValuePair.getValue()).getValue();
													}
												}
											}
											else if (annotation instanceof SingleMemberAnnotationExpr) {
												uri = ((SingleMemberAnnotationExpr)annotation).getMemberValue().toString();
											}
											operation = new RESTfulHttpOperation(uri);
											apiDescription.addOperations(new RESTfulHttpOperation(currentMethod.getName()));
											break annotation;
										}
									}
								}
							}
						}
					}
				}
			}
			// System.out.println(Arrays.toString(classAnnotations.toArray(new
			// String[classAnnotations.size()])));
			// repoClassesAnnotations.add(classAnnotations.toArray(new
			// String[classAnnotations.size()]));
		} catch (FileNotFoundException | ParseException e) {
			System.out.println(e.getMessage());
		}

		return apiDescription;
	}

	public void fetchRepositoryContent(String repository, String localRepoDir) {
		CloneCommand cloneCommand = null;
		File localRepo = new File(localRepoDir);
		cloneCommand = Git.cloneRepository().setDirectory(localRepo).setURI(repository);

		try {
			cloneCommand.call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		cloneCommand.setDirectory(null);
	}

	private void getJavaFilePathFromDirectory(File file, List<String> path) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				getJavaFilePathFromDirectory(f, path);
			}
		} else if (file.getName().endsWith(".java")) {
			path.add(file.getPath());
		}
	}

	private boolean isClassRestController(CompilationUnit cu) {
		ClassOrInterfaceDeclaration classOrInterface = null;
		boolean isRestController = false;
		LinkedList<Node> elementsToAnalyse = new LinkedList<Node>();
		elementsToAnalyse.add(cu);
		while (!elementsToAnalyse.isEmpty() && classOrInterface == null) {
			if (elementsToAnalyse.peek() instanceof ClassOrInterfaceDeclaration) {
				classOrInterface = (ClassOrInterfaceDeclaration) elementsToAnalyse.peek();
			} else {
				elementsToAnalyse.addAll(elementsToAnalyse.peek().getChildrenNodes());
				elementsToAnalyse.remove();
			}
		}

		if (classOrInterface != null) {

		}

		return isRestController;
		// if(classOrInterface.getAnnotations().contains(REST_CONTROLLER_ANNOTATION))
		// if(child.toString().contains(REST_CONTROLLER_ANNOTATION)) {
		// String nodeText = child.toString();
		// System.out.println(nodeText);
		// Pattern p = Pattern.compile("class +.* *|\\{");
		// Matcher matcher = p.matcher(nodeText);
		// if(matcher.find()) {
		// System.out.println(nodeText.subSequence(matcher.start(),
		// matcher.end()).toString());
		// names.add(nodeText.subSequence(matcher.start(),
		// matcher.end()).toString());
		// } else {
		// System.out.println("not found");
		// }
		// }
	}

	public void deleteDir(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				deleteDir(f);
			}
		}
		file.delete();
	}
}
