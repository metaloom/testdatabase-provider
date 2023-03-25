package io.metaloom.test.container.provider.model;

public class DatabasePoolResponse extends AbstractDatabasePoolModel {

  private int level;

  private int allocationLevel;
  private boolean started;

  public int getLevel() {
    return level;
  }

  public DatabasePoolResponse setLevel(int level) {
    this.level = level;
    return this;
  }

  public int getAllocationLevel() {
    return allocationLevel;
  }

  public DatabasePoolResponse setAllocationLevel(int allocationLevel) {
    this.allocationLevel = allocationLevel;
    return this;
  }

  public boolean isStarted() {
    return started;
  }

  public DatabasePoolResponse setStarted(boolean started) {
    this.started = started;
    return this;
  }

}
