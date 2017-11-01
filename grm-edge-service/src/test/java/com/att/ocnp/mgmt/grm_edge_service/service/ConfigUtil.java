/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.ocnp.mgmt.grm_edge_service.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ConfigUtil {

	  private static final String configFileRootDirectory = System.getProperty( "user.dir" ) + "/src/test/resources";

	  public static void copyFile( String sourceFileName, String destFileName ) throws IOException {
	    File sourceFile = new File( configFileRootDirectory + "/" + sourceFileName );
	    File destFile = new File( configFileRootDirectory + "/" + destFileName );
	    System.out.println("Source File absolute path" + sourceFile.getAbsolutePath());
	    System.out.println("Dest File absolute path" + destFile.getAbsolutePath());
	    
	    if ( !sourceFile.exists() ) {
	      return;
	    }
	    if ( !destFile.exists() ) {
	      destFile.createNewFile();
	    }
	    FileChannel source = null;
	    FileChannel destination = null;
	    source = new FileInputStream( sourceFile ).getChannel();
	    destination = new FileOutputStream( destFile ).getChannel();
	    if ( destination != null && source != null ) {
	      destination.transferFrom( source, 0, source.size() );
	    }
	    if ( source != null ) {
	      source.close();
	    }
	    if ( destination != null ) {
	      destination.close();
	    }
	    System.out.println("File copied successfully - absolute paths of source - " + sourceFile.getAbsolutePath());
	    System.out.println("File copied successfully - absolute paths of dest - " + destFile.getAbsolutePath());

	  }
	
}
