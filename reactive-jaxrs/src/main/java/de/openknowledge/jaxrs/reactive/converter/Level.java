/*
 *
 * de.openknowledge.jaxrs.reactive.converter.Level
 *
 *
 * This document contains trade secret data which is the property of OpenKnowledge GmbH. Information contained herein
 * may not be used, copied or disclosed in whole or part except as permitted by written agreement from open knowledge
 * GmbH.
 *
 * Copyright (C) 2017 open knowledge GmbH / Oldenburg / Germany
 *
 */
package de.openknowledge.jaxrs.reactive.converter;

/**
 * @author Robert Zilke - open knowledge GmbH
 */
public class Level {

  private int depth;

  public Level() {

    depth = 0;
  }

  public void increment() {

    depth++;
  }

  public void decrement() {

    if (depth == 0){
      throw new IllegalStateException("Root level reached");
    }

    depth--;
  }

  public boolean isOnRootLevel(){
    return depth == 0;
  }
}
