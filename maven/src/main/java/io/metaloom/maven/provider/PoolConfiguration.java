package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolConfiguration {

  @Parameter
  private String id;

  @Parameter
  private String templateName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }
}
