package actions.model;

import tree.Tree;

public class MoveIn extends TreeAddition {
    private String srcFile;
    public MoveIn(Tree node, Tree parent, String srcFile, int pos) {
        super(node, parent, pos);
        this.srcFile = srcFile;
    }

    public String getSrcFile() {
        return srcFile;
    }

    @Override
    public String getName() {
        return "M";
    }

    @Override
    public String toString()  {

        return "Moved from File: " + getSrcFile();
    }
}
