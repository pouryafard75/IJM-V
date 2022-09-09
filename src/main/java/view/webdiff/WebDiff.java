/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package view.webdiff;

import actions.ASTDiff;
import io.DirectoryComparator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.Diff;
import org.eclipse.jdt.core.dom.AST;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;
import spark.Spark;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

public class WebDiff {
    public static final String JQUERY_JS_URL = "https://code.jquery.com/jquery-3.4.1.min.js";
    public static final String BOOTSTRAP_CSS_URL = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css";
    public static final String BOOTSTRAP_JS_URL = "https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js";
    private String srcFilePath,dstFilePath;
    private ASTDiff astDiff;
    public WebDiff(String srcFilePath, String dstFilePath, ASTDiff astDiff) {
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;
        this.astDiff = astDiff;
    }


    public void run() {
        int port = 6789;
        DirectoryComparator comparator = new DirectoryComparator(srcFilePath, dstFilePath);
        comparator.compare();
        configureSpark(comparator,port);
        Spark.awaitInitialization();
        System.out.println(String.format("Starting server: %s:%d.", "http://127.0.0.1", port));
    }

    public void configureSpark(DirectoryComparator comparator, int port) {
        port(port);
        staticFiles.location("/web/");
        get("/", (request, response) -> {
                response.redirect("/vanilla-diff/0");
            return "";
        });
        get("/vanilla-diff/:id", (request, response) -> {
            int id = Integer.parseInt(request.params(":id"));
            Pair<File, File> pair = comparator.getModifiedFiles().get(id);
//            Diff diff = getDiff(pair.first.getAbsolutePath(), pair.second.getAbsolutePath());
            String srcContent = FileUtils.readFileToString(new File(srcFilePath));
            String dstContent = FileUtils.readFileToString(new File(dstFilePath));
            Renderable view = new VanillaDiffView("","", srcContent,dstContent,astDiff, false);
            return render(view);
        });
//        get("/monaco-diff/:id", (request, response) -> {
//            int id = Integer.parseInt(request.params(":id"));
//            Pair<File, File> pair = comparator.getModifiedFiles().get(id);
////            Diff diff = getDiff(pair.first.getAbsolutePath(), pair.second.getAbsolutePath());
//            Renderable view = new MonacoDiffView(pair.first, pair.second, nu, id);
//            return render(view);
//        });

        get("/left/:id", (request, response) -> {
            int id = Integer.parseInt(request.params(":id"));
            Pair<File, File> pair = comparator.getModifiedFiles().get(id);
            return readFile(pair.first.getAbsolutePath(), Charset.defaultCharset());
        });
        get("/right/:id", (request, response) -> {
            int id = Integer.parseInt(request.params(":id"));
            Pair<File, File> pair = comparator.getModifiedFiles().get(id);
            return readFile(pair.second.getAbsolutePath(), Charset.defaultCharset());
        });
        get("/quit", (request, response) -> {
            System.exit(0);
            return "";
        });
    }

    private static String render(Renderable r) throws IOException {
        HtmlCanvas c = new HtmlCanvas();
        r.renderOn(c);
        return c.toHtml();
    }

    private static String readFile(String path, Charset encoding)  throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
