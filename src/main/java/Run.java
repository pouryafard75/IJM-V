import actions.ASTDiff;
import actions.EditScript;
import actions.model.Action;
import actions.model.Delete;
import actions.model.Insert;
import actions.model.Move;
import jdt.JdtVisitor;
import tree.Tree;
import tree.TreeContext;


import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.dom.AST;
import org.apache.commons.io.FileUtils;

import org.eclipse.jdt.core.dom.ASTParser;

import org.eclipse.jdt.core.dom.CompilationUnit;
import view.webdiff.WebDiff;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Run {
    public static void main(String[] args) {
        String srcFilePath = "D:\\TestCases\\v1\\DistributedCacheStream.java";
        String dstFilePath = "D:\\TestCases\\v2\\DistributedCacheStream.java";
        String csvPath = "test.csv";
        Tree srcTree = getTreeByFilePath(srcFilePath);
        Tree dstTree = getTreeByFilePath(dstFilePath);
        List<IJMActionModel> ijmActions = makeActionsFromCSV(csvPath);
        EditScript editScript = makeActionFromIJMAction(ijmActions,srcTree,dstTree);

        TreeContext srcTreeContext = new TreeContext();
        srcTreeContext.setRoot(srcTree);
        srcTreeContext.setFilename(srcFilePath);

        TreeContext dstTreeContext = new TreeContext();
        dstTreeContext.setRoot(dstTree);
        dstTreeContext.setFilename(dstFilePath);

        ASTDiff astDiff = new ASTDiff(srcTreeContext,dstTreeContext,null);
        astDiff.setEditScript(editScript);

        WebDiff webDiff = new WebDiff(srcFilePath,dstFilePath,astDiff,csvPath);
        webDiff.run();

    }

    private static Tree getTreeByFilePath(String fullPath) {
        String javaFileContent = null;
        try {
            javaFileContent = FileUtils.readFileToString(new File(fullPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        parser.setCompilerOptions(options);
        parser.setResolveBindings(false);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setStatementsRecovery(true);
        parser.setSource(javaFileContent.toCharArray());
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        IScanner scanner = ToolFactory.createScanner(true, false, false, false);
        scanner.setSource(javaFileContent.toCharArray());
        JdtVisitor visitor = new JdtVisitor(scanner);
        compilationUnit.accept(visitor);
        TreeContext treeContext = visitor.getTreeContext();
        return treeContext.getRoot();
    }

    private static EditScript makeActionFromIJMAction(List<IJMActionModel> ijmActions , Tree src, Tree dst){
        EditScript editScript = new EditScript();
        Tree srcCopy = src.deepCopy();
        Tree dstCopy = dst.deepCopy();
        for (IJMActionModel ijmAction : ijmActions) {
            Action action = null;
            Tree srcTree = src.getTreeBetweenPositions(ijmAction.srcStartOffset, ijmAction.srcEndOffset);
            Tree dstTree = dst.getTreeBetweenPositions(ijmAction.dstStartOffset, ijmAction.dstEndOffset);
            switch (ijmAction.getActionType()) {
                case "INS":
                    action = new Insert(dstTree, null, -1);
                    break;
                case "MOV":
                    System.out.println("");
                    action = new Move(srcTree, dstTree, -1);
                    break;
                case "DEL":
                    action = new Delete(srcTree);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + ijmAction.getActionType());
            }
            editScript.add(action);
        }
        return editScript;
    }


    private static List<IJMActionModel> makeActionsFromCSV(String fileName) {
        List<IJMActionModel> actions = new ArrayList<>();


        try (BufferedReader br = Files.newBufferedReader(Path.of(fileName), StandardCharsets.US_ASCII)) {
            String line = br.readLine();
            int counter = 0;
            while (line != null) {
                counter += 1;
                if (counter == 1)
                {
                    line = br.readLine();
                    continue;
                }
                String[] attributes = line.split(";");
                actions.add(createIJMAction(attributes));
                line = br.readLine();


            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return actions;
    }

    private static IJMActionModel createIJMAction(String[] metadata) {
        IJMActionModel ijmActionModel = new IJMActionModel();
        ijmActionModel.setActionType(metadata[2]);
        ijmActionModel.setSrcStartOffset(Integer.valueOf(metadata[9]).intValue());
        ijmActionModel.setSrcEndOffset(Integer.valueOf(metadata[10]).intValue());
        ijmActionModel.setDstStartOffset(Integer.valueOf(metadata[11]).intValue());
        ijmActionModel.setDstEndOffset(Integer.valueOf(metadata[12]).intValue());
        return ijmActionModel;
    }
}

class IJMActionModel{
    String actionType;
    int srcStartOffset;
    int srcEndOffset;
    int dstStartOffset;
    int dstEndOffset;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getSrcStartOffset() {
        return srcStartOffset;
    }

    public void setSrcStartOffset(int srcStartOffset) {
        this.srcStartOffset = srcStartOffset;
    }

    public int getSrcEndOffset() {
        return srcEndOffset;
    }

    public void setSrcEndOffset(int srcEndOffset) {
        this.srcEndOffset = srcEndOffset;
    }

    public int getDstStartOffset() {
        return dstStartOffset;
    }

    public void setDstStartOffset(int dstStartOffset) {
        this.dstStartOffset = dstStartOffset;
    }

    public int getDstEndOffset() {
        return dstEndOffset;
    }

    public void setDstEndOffset(int dstEndOffset) {
        this.dstEndOffset = dstEndOffset;
    }
}
