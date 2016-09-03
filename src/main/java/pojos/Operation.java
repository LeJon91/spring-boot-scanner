package pojos;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class Operation {

	private String returnValue;
	private String operationName;
	private MultiValueMap<String,Object> parameter;
	
	public Operation(String returnValue, String operationName, MultiValueMap<String,Object> parameterList) {
		this.returnValue = returnValue;
		this.operationName = operationName;
		this.parameter = parameterList;
	}
	
	public Operation () {
		returnValue = "";
		operationName = "";
		parameter = new LinkedMultiValueMap<String,Object>();
	}
	public Object getReturnValue() {
		return returnValue;
	}
	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	public String getOperationName() {
		return operationName;
	}
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}
	public MultiValueMap<String, Object> getParameter() {
		return parameter;
	}
	public void setParameter(MultiValueMap<String, Object> parameter) {
		this.parameter = parameter;
	}
	@Override
	public String toString() {
		String parameterList = "";
		if(parameter != null) {
			String[] keySet = parameter.keySet().toArray(new String[parameter.size()]);
			for(String key:keySet) {
				parameterList+=key + ":" + parameter.get(key).getClass().getName();
			}
		}
		return operationName + "(" + parameterList + ")" + ":" + returnValue;
	}
}
