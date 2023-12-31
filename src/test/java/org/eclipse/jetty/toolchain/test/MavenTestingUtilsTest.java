//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.toolchain.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("javadoc")
public class MavenTestingUtilsTest
{
    @Test
    public void testGetTargetDir()
    {
        File dir = MavenTestingUtils.getTargetDir();
        assertEquals("target",dir.getName());
    }

    @Test
    public void testGetTargetFileBasic() throws IOException
    {
        File dir = MavenTestingUtils.getTargetDir();

        // File in .../target/pizza.log
        File expected = new File(dir,"pizza.log");
        FS.touch(expected);

        File actual = MavenTestingUtils.getTargetFile("pizza.log");
        assertSamePath(expected,actual);
    }

    @Test
    public void testGetTargetFileDeep() throws IOException
    {
        File dir = MavenTestingUtils.getTargetDir();
        File pizzadir = new File(dir,"pizza");
        FS.ensureDirExists(pizzadir);

        // File in .../target/pizza/pizza.receipt
        File expected = new File(pizzadir,"pizza.receipt");
        FS.touch(expected);

        // Should automatically adjust deep path
        File actual = MavenTestingUtils.getTargetFile("pizza/pizza.receipt");
        assertSamePath(expected,actual);
    }

    @Test
    public void testGetTestResourceFileSimple()
    {
        File dir = MavenTestingUtils.getTestResourcesDir();
        File expected = new File(dir,"dessert.txt");
        File actual = MavenTestingUtils.getTestResourceFile("dessert.txt");
        assertSamePath(expected,actual);
    }

    @Test
    public void testGetTestResourceFileDeep()
    {
        File dir = MavenTestingUtils.getTestResourcesDir();
        File breakfast = new File(dir,"breakfast");
        File expected = new File(breakfast,"eggs.txt");
        File actual = MavenTestingUtils.getTestResourceFile("breakfast/eggs.txt");
        assertSamePath(expected,actual);
    }

    @Test
    public void testGetTargetURL() throws Exception
    {
        File dir = MavenTestingUtils.getTargetDir();

        // File in .../target/pizza.log
        File expected = new File(dir,"url.log");
        FS.touch(expected);
        
        File actual = MavenTestingUtils.getTargetFile("url.log");
        
        URL url = MavenTestingUtils.getTargetURL("url.log");
        
        assertEquals( actual.toURI().toURL().toExternalForm(), url.toExternalForm());        
    }
    
    @Test
    public void testGetTestResourceDir()
    {
        File dir = MavenTestingUtils.getTestResourcesDir();
        File expected = new File(dir,"breakfast");
        File actual = MavenTestingUtils.getTestResourceDir("breakfast");
        assertSamePath(expected,actual);
    }
    
    private void assertSamePath(File expected, File actual)
    {
        assertEquals(expected.getAbsolutePath(),actual.getAbsolutePath());
    }
}
