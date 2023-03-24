package io.metaloom.maven.provider;

import org.apache.maven.plugins.annotations.Parameter;

public class PoolConfiguration {

  @Parameter
  private String id;

  @Parameter
  private String templateName;

  @Parameter
  private int minimum;

  @Parameter
  private int maximum;

  @Parameter
  private int increment;

  public String getId() {
    return id;
  }

  public String getTemplateName() {
    return templateName;
  }

  public int getMinimum() {
    return minimum;
  }

  public int getMaximum() {
    return maximum;
  }

  public int getIncrement() {
    return increment;
  }

}
