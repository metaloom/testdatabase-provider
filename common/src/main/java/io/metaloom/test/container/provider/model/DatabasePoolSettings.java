package io.metaloom.test.container.provider.model;

public class DatabasePoolSettings {

  private Integer increment;
  private Integer minimum;
  private Integer maximum;

  public Integer getMinimum() {
    return minimum;
  }

  public DatabasePoolSettings setMinimum(Integer minimum) {
    this.minimum = minimum;
    return this;
  }

  public Integer getMaximum() {
    return maximum;
  }

  public DatabasePoolSettings setMaximum(Integer maximum) {
    this.maximum = maximum;
    return this;
  }

  public Integer getIncrement() {
    return increment;
  }

  public DatabasePoolSettings setIncrement(Integer increment) {
    this.increment = increment;
    return this;
  }
}
