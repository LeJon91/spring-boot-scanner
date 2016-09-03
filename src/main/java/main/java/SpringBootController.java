package main.java;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import API.APIDescription;
import API.RESTfulHttpOperation;
import analysis.SpringBootAnalysis;


@RestController	
public class SpringBootController {

	@Value("${localRepository}")
	private String localRepo;

	@RequestMapping(path = "/interface", method = RequestMethod.GET)
	public ResponseEntity<String> getInterface(@RequestParam(required = true) String repoParam,
			@RequestParam(required = false) String msFinderService) {
		String repoURI = "";
		String msFinderURI = "";

		try {
			if(repoURI != null) {
				repoURI = new String(Base64.decodeBase64(URLDecoder.decode(repoParam, "UTF-8").getBytes()));
			}
			
			if(msFinderService != null) {
				msFinderURI = new String(Base64.decodeBase64(URLDecoder.decode(msFinderService, "UTF-8").getBytes()));
			}
		} catch (UnsupportedEncodingException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

//		//find target jar from POM
//		RequestEntity<String> requestEntity = new RequestEntity<String>(HttpMethod.GET,
//				URI.create(MAVEN_SCANNER_URI + msFinderURI + "?repo=" + repoParam));
//		ResponseEntity<Service> results = new RestTemplate().exchange(requestEntity, Service.class);
//		if (results.getStatusCode().equals(HttpStatus.OK)) {
//			System.out.println(results.getBody());
//		} else {
//			System.out.println("FAILED");
//		}

		//find interface
		SpringBootAnalysis analysis = new SpringBootAnalysis();
		analysis.fetchRepositoryContent(repoURI,localRepo);
		APIDescription serviceInterface = analysis.getServiceInterface(localRepo);
		analysis.deleteDir(new File(localRepo));
		return new ResponseEntity<String>("Repo URI: " + repoURI + ", msFinderURI: " + msFinderURI, HttpStatus.OK);
	}
	
	@RequestMapping(path = "/debugService")
	public ResponseEntity<String> debugService(@RequestParam(required = true) String pathToLocalRepo) {
		SpringBootAnalysis analysis = new SpringBootAnalysis();
		APIDescription serviceInterface = analysis.getServiceInterface(pathToLocalRepo);
		for(RESTfulHttpOperation op:serviceInterface.getOperations()) {
			System.out.println(op.getPath());
		}
		return new ResponseEntity<String>("+++FERTIG+++", HttpStatus.OK);
	}

}
