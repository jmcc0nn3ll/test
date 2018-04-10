//
//  ========================================================================
//  Copyright (c) 1995-2018 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.toolchain.test;

import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

/**
 * Common FileSystem utility methods
 */
public final class FS
{
    private FS()
    {
        /* prevent instantiation */
    }

    /**
     * Delete a file or a directory.
     * <p>
     * Note: safety mechanism only allows delete within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param path
     *            the file or directory to delete.
     */
    public static void delete(Path path)
    {
        if (!Files.exists(path))
        {
            return; // nothing to delete. we're done.
        }

        if (Files.isRegularFile(path))
        {
            deleteFile(path);
        }
        else if (Files.isDirectory(path))
        {
            deleteDirectory(path);
        }
        else
        {
            Assert.fail("Not able to delete path, not a file or directory? : " + path.toAbsolutePath());
        }
    }

    /**
     * Delete a file or a directory.
     * <p>
     * Note: safety mechanism only allows delete within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param path
     *            the file or directory to delete.
     */
    public static void delete(File path)
    {
        delete(path.toPath());
    }

    /**
     * Delete a directory and all contents under it.
     * <p>
     * Note: safety mechanism only allows delete directory within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param dir
     *            the directory to delete.
     */
    public static void deleteDirectory(File dir)
    {
        deleteDirectory(dir.toPath());
    }

    /**
     * Delete a directory and all contents under it.
     * <p>
     * Note: safety mechanism only allows delete directory within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param dir
     *            the directory to delete.
     */
    public static void deleteDirectory(Path dir)
    {
        recursiveDeleteDir(dir);
    }

    /**
     * Delete a file.
     * <p>
     * Note: safety mechanism only allows delete file within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param path
     *            the path to delete.
     */
    public static void deleteFile(File path)
    {
        Assert.assertTrue("Path must be a file: " + path.getAbsolutePath(),path.isFile());
        Assert.assertTrue("Can only delete content within the /target/tests/ directory: " + path.getAbsolutePath(),FS.isTestingDir(path.getParentFile()));

        Assert.assertTrue("Failed to delete file: " + path.getAbsolutePath(),path.delete());
    }

    /**
     * Delete a file.
     * <p>
     * Note: safety mechanism only allows delete file within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param path
     *            the path to delete.
     */
    public static void deleteFile(Path path)
    {
        String location = path.toAbsolutePath().toString();

        if (Files.exists(path,LinkOption.NOFOLLOW_LINKS))
        {
            Assert.assertTrue("Path must be a file or link: " + location,Files.isRegularFile(path) || Files.isSymbolicLink(path));
            Assert.assertTrue("Can only delete content within the /target/tests/ directory: " + location,FS.isTestingDir(path.getParent()));
            try
            {
                Assert.assertTrue("Failed to delete file: " + location,Files.deleteIfExists(path));
            }
            catch (IOException e)
            {
                throw new AssertionError("Unable to delete file: " + location,e);
            }
        }
    }

    /**
     * Delete a directory. (only if it is empty)
     * <p>
     * Note: safety mechanism only allows delete file within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param path
     *            the path to delete.
     */
    public static void deleteDir(Path path)
    {
        String location = path.toAbsolutePath().toString();

        if (Files.exists(path))
        {
            Assert.assertTrue("Path must be a file: " + location,Files.isDirectory(path));
            Assert.assertTrue("Can only delete content within the /target/tests/ directory: " + location,FS.isTestingDir(path.getParent()));
            try
            {
                Assert.assertTrue("Failed to delete directory: " + location,Files.deleteIfExists(path));
            }
            catch (IOException e)
            {
                throw new AssertionError("Unable to delete directory: " + location,e);
            }
        }
    }

    private static void recursiveDeleteDir(Path path)
    {
        String location = path.toAbsolutePath().toString();
        Assert.assertTrue("Can only delete content within the /target/tests/ directory: " + location,FS.isTestingDir(path));

        // Get entries in this path
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(path))
        {
            for (Path entry : dir)
            {
                if (Files.isDirectory(entry))
                {
                    recursiveDeleteDir(entry);
                } 
                else
                {
                    deleteFile(entry);
                }
            }
        }
        catch (DirectoryIteratorException e)
        {
            throw new AssertionError("Unable to (recursively) delete path: " + location, e);
        }
        catch (IOException e)
        {
            throw new AssertionError("Unable to (recursively) delete path: " + location, e);
        }

        // delete itself
        deleteDir(path);
    }

    /**
     * Delete the contents of a directory and all contents under it, leaving the directory itself still in existance.
     * <p>
     * Note: safety mechanism only allows clean directory within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param dir
     *            the directory to delete.
     */
    public static void cleanDirectory(File dir)
    {
        cleanDirectory(dir.toPath());
    }

    /**
     * Delete the contents of a directory and all contents under it, leaving the directory itself still in existance.
     * <p>
     * Note: safety mechanism only allows clean directory within the {@link MavenTestingUtils#getTargetTestingDir()} directory.
     * 
     * @param dir
     *            the directory to delete.
     */
    public static void cleanDirectory(Path dir)
    {
        deleteDirectory(dir);
        ensureDirExists(dir);
    }

    /**
     * Ensure the provided directory exists, and contains no content (empty)
     * 
     * @param dir
     *            the dir to check.
     */
    public static void ensureEmpty(File dir)
    {
        ensureEmpty(dir.toPath());
    }

    /**
     * Ensure the provided directory exists, and contains no content (empty)
     * 
     * @param dir
     *            the dir to check.
     */
    public static void ensureEmpty(Path dir)
    {
        if (Files.exists(dir))
        {
            FS.cleanDirectory(dir);
        }
        else
        {
            FS.ensureDirExists(dir);
        }
    }

    /**
     * Ensure the provided directory exists, and contains no content (empty)
     * 
     * @param testingdir
     *            the dir to check.
     */
    public static void ensureEmpty(TestingDir testingdir)
    {
        ensureEmpty(testingdir.getPath());
    }

    /**
     * Ensure the provided directory does not exist, delete it if present
     * 
     * @param dir
     *            the dir to check
     */
    public static void ensureDeleted(File dir)
    {
        ensureDeleted(dir.toPath());
    }

    /**
     * Ensure the provided directory does not exist, delete it if present
     * 
     * @param dir
     *            the dir to check
     */
    public static void ensureDeleted(Path dir)
    {
        if (Files.exists(dir))
        {
            FS.deleteDirectory(dir);
        }
    }

    /**
     * Ensure that directory exists, create it if not present. Leave it alone if already there.
     * 
     * @param dir
     *            the dir to check.
     */
    public static void ensureDirExists(File dir)
    {
        if (dir.exists())
        {
            Assert.assertTrue("Path exists, but should be a Dir : " + dir.getAbsolutePath(),dir.isDirectory());
        }
        else
        {
            Assert.assertTrue("Creating dir: " + dir,dir.mkdirs());
        }
    }

    /**
     * Ensure that directory exists, create it if not present. Leave it alone if already there.
     * 
     * @param dir
     *            the dir to check.
     */
    public static void ensureDirExists(Path dir)
    {
        if (Files.exists(dir))
        {
            Assert.assertTrue("Path exists, but should be a Dir : " + dir.toAbsolutePath(),Files.isDirectory(dir));
        }
        else
        {
            try
            {
                Files.createDirectories(dir);
                Assert.assertTrue("Failed to create dir: " + dir,Files.exists(dir));
            }
            catch (IOException e)
            {
                throw new AssertionError("Failed to create directory: " + dir,e);
            }
        }
    }

    /**
     * Internal class used to detect if the directory is a valid testing directory.
     * <p>
     * Used as part of the validation on what directories are safe to delete from.
     * 
     * @param dir
     *            the dir to check
     * @return true if provided directory is a testing directory
     */
    protected static boolean isTestingDir(File dir)
    {
        return isTestingDir(dir.toPath());
    }

    /**
     * Internal class used to detect if the directory is a valid testing directory.
     * <p>
     * Used as part of the validation on what directories are safe to delete from.
     * 
     * @param dir
     *            the dir to check
     * @return true if provided directory is a testing directory
     */
    protected static boolean isTestingDir(Path dir)
    {
        try
        {
            return dir.toRealPath().startsWith(MavenTestingUtils.getTargetTestingPath());
        }
        catch (IOException e)
        {
            // Fallback when toRealPath() fails (on some filesystems)
            return dir.toAbsolutePath().startsWith(MavenTestingUtils.getTargetTestingPath());
        }
    }

    /**
     * Create an empty file at the location. If the file exists, just update the last modified timestamp.
     * 
     * @param file
     *            the file to create or update the timestamp of.
     * @throws IOException
     *             if unable to create the new file.
     */
    public static void touch(File file) throws IOException
    {
        if (file.exists())
        {
            Assert.assertTrue("Updating last modified timestamp",file.setLastModified(System.currentTimeMillis()));
        }
        else
        {
            Assert.assertTrue("Creating file: " + file,file.createNewFile());
        }
    }

    /**
     * Create an empty file at the location. If the file exists, just update the last modified timestamp.
     * 
     * @param file
     *            the file to create or update the timestamp of.
     * @throws IOException
     *             if unable to create the new file.
     */
    public static void touch(Path file) throws IOException
    {
        if (Files.exists(file))
        {
            FileTime timeOrig = Files.getLastModifiedTime(file);
            Files.setLastModifiedTime(file,FileTime.from(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
            FileTime timeNow = Files.getLastModifiedTime(file);
            // Verify that timestamp was actually updated.
            Assert.assertThat("Timestamp updated",timeOrig,not(equalTo(timeNow)));
        }
        else
        {
            Files.createFile(file);
            Assert.assertTrue("Created new file?: " + file,Files.exists(file));
        }
    }
}
