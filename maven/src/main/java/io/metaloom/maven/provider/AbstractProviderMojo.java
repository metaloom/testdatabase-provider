package io.metaloom.maven.provider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import io.vertx.core.Vertx;

public abstract class AbstractProviderMojo extends AbstractMojo {

  public static final Vertx vertx = Vertx.vertx();

  @Parameter(defaultValue = "${project}", required = true, readonly = true)
  MavenProject project;

}
