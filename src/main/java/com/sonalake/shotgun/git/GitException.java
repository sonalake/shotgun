package com.sonalake.shotgun.git;

public class GitException extends RuntimeException {
  public GitException(String message) {
    super(message);
  }

  public GitException(String message, Throwable cause) {
    super(message, cause);
  }
}
