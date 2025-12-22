/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.fs.smbfs;

import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


public class SmbShell extends NtlmAuthenticator {

    @Override
    protected NtlmPasswordAuthenticator getNtlmPasswordAuthentication() {
        System.out.println(getRequestingException().getMessage() +
            " for " + getRequestingURL());
        System.out.print("username: ");
        try {
            int i;
            String username = readLine();
            String domain = null, password;

            if ((i = username.indexOf('\\')) != -1) {
                domain = username.substring(0, i);
                username = username.substring(i + 1);
            }
            System.out.print("password: ");
            password = readLine();
            if (password.isEmpty()) {
                return null;
            }
            return new NtlmPasswordAuthenticator(domain, username, password);
        } catch (Exception e) {
            // empty
        }
        return null;
    }

    public static String readLine() throws Exception {
        int c;
        StringBuilder sb = new StringBuilder();
        while ((c = System.in.read()) != '\n') {
            if (c == -1) return "";
            sb.append((char) c);
        }
        return sb.toString().trim();
    }

    final String start;

    public SmbShell(String start) {
        this.start = start;
        NtlmAuthenticator.setDefault(this);
    }

    void run() throws Exception {
        String cmd, prompt;
        SmbFile conn, tmp;
        SimpleDateFormat sdf1 = new SimpleDateFormat("EEE MMM");
        SimpleDateFormat sdf2 = new SimpleDateFormat("d");
        SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy h:mm a");
        sdf1.setCalendar(new GregorianCalendar());
        sdf2.setCalendar(new GregorianCalendar());
        sdf3.setCalendar(new GregorianCalendar());

        CIFSContext context = SingletonContext.getInstance().withCredentials(getNtlmPasswordAuthentication());
        conn = new SmbFile(start, context);
        while (true) {
            try {
                if (conn.exists()) {
                    prompt = conn.getName() + "> ";
                } else {
                    System.out.println("error reading " + conn);
                    conn = new SmbFile("smb://", context);
                    continue;
                }
                System.out.print(prompt);

                cmd = readLine();
                if (cmd.isEmpty()) {
                    // empty
                } else if (cmd.startsWith("cd")) {
                    int i = cmd.indexOf(' ');
                    String dir;
                    if (i == -1 || (dir = cmd.substring(i).trim()).isEmpty()) {
                        conn = new SmbFile("smb://", context);
                        continue;
                    }
                    tmp = new SmbFile(conn, dir);
                    if (tmp.exists()) {
                        if (tmp.isDirectory()) {
                            conn = tmp;
                        } else {
                            System.out.println(dir + " is not a directory");
                        }
                    } else {
                        System.out.println("no such directory");
                    }
                } else if (cmd.startsWith("ls")) {
                    int i = cmd.indexOf(' ');
                    SmbFile d = conn;
                    String dir, wildcard = "*";
                    if (i != -1 && !(dir = cmd.substring(i).trim()).isEmpty()) {
                        // there's an argument which could be a directory,
                        // a wildcard, or a directory with a wildcard appended
                        int s = dir.lastIndexOf('/');
                        int a = dir.lastIndexOf('*');
                        int q = dir.lastIndexOf('?');

                        if ((a != -1 && a > s) || (q != -1 && q > s)) {
                            // it's a wildcard
                            if (s == -1) {
                                wildcard = dir;
                                d = conn;
                            } else {
                                wildcard = dir.substring(s + 1);
                                d = new SmbFile(conn, dir.substring(0, s));
                            }
                        } else {
                            d = new SmbFile(conn, dir);
                        }
                    }
                    long t0 = System.currentTimeMillis();
                    SmbFile[] list = d.listFiles(wildcard);
                    t0 = System.currentTimeMillis() - t0;
                    if (list != null) {
                        for (SmbFile smbFile : list) {
                            StringBuilder sb = new StringBuilder();
                            Date date = new Date(smbFile.lastModified());
                            System.out.printf("%-40s", smbFile.getName());
                            sb.append(smbFile.isDirectory() ? 'd' : '-');
                            sb.append(smbFile.canRead() ? 'r' : '-');
                            sb.append(smbFile.canWrite() ? 'w' : '-');
                            sb.append(smbFile.isHidden() ? 'h' : '-');
                            sb.append(smbFile.getType() == SmbFile.TYPE_WORKGROUP ? 'g' : '-');
                            System.out.printf("%-6s", sb);
                            System.out.printf("%10d ", smbFile.length());

                            System.out.print(sdf1.format(date));
                            System.out.printf("%3s ", sdf2.format(date));
                            System.out.print(sdf3.format(date));
                            System.out.println();
                        }
                        System.out.println(list.length + " items in " + t0 + "ms");
                    } else {
                        System.out.println("no such file or directory");
                    }
                } else if (cmd.startsWith("pwd")) {
                    System.out.println(conn.getCanonicalPath());
                } else if (cmd.startsWith("q") ||
                    cmd.startsWith("x") ||
                    cmd.startsWith("ex") ||
                    cmd.startsWith("by")) {
                    break;
                } else {
                    System.out.println("commands:");
                    System.out.println("  ls [dir|file]");
                    System.out.println("  cd dir");
                    System.out.println("  pwd");
                    System.out.println("  quit");
                }
            } catch (MalformedURLException mue) {
                mue.printStackTrace(System.err);
                conn = null;
            } catch (SmbException se) {
                se.printStackTrace(System.err);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
        System.exit(0);
    }

    public static void main(String[] argv) throws Exception {
        SmbShell smbsh = new SmbShell(argv.length > 0 ? argv[0] : "smb://");
        smbsh.run();
    }
}
