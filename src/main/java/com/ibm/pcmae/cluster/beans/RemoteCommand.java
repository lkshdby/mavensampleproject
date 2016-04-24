package com.ibm.pcmae.cluster.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class that stores parameters of a command to be executed
 * on a remote cluster machine.
 * 
 * @author Marcos Dias de Assuncao
 */

@XmlRootElement
public class RemoteCommand {
	private String machineId;
	private String command;
	private String state;
	private String startTime;
	private String finishTime;
	private int returnCode;
	private String exitReason;
	private String outputPath;
	private String errorPath;
	private String remoteFilePath;
	private byte[] fileUpload;
	private String output;
	private String error;

	public RemoteCommand() {
		
	}

	public String getMachineId() {
		return machineId;
	}

	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}

	public String getErrorPath() {
		return errorPath;
	}

	public void setErrorPath(String errorPath) {
		this.errorPath = errorPath;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public String getExitReason() {
		return exitReason;
	}

	public void setExitReason(String exitReason) {
		this.exitReason = exitReason;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	public void setRemoteFilePath(String remoteFilePath) {
		this.remoteFilePath = remoteFilePath;
	}

	public byte[] getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(byte[] fileUpload) {
		this.fileUpload = fileUpload;
	}
}
