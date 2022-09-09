
package actions;
import actions.model.MultiMoveActionGenerator;
import matchers.MultiMappingStore;
import tree.Tree;
import tree.TreeContext;

import java.util.Map;
import java.util.Set;

/**
 * Class to facilitate the computation of diffs between ASTs.
 */
public class ASTDiff {

//    public String srcPath;
//    public String dstPath;

    private TreeContext srcTC;

    private TreeContext dstTC;

    private MultiMappingStore mappings;

    /**
     * The edit script between the two ASTs.
     */
    private EditScript editScript = new EditScript();

    public TreeContext getDstTC() {
        return dstTC;
    }

    public TreeContext getSrcTC() {
        return srcTC;
    }

    public MultiMappingStore getMappings() {
        return mappings;
    }

    public EditScript getEditScript() {
        return editScript;
    }
    public void setEditScript(EditScript editScript) {
        this.editScript = editScript;
    }

    /**
     * Instantiate a diff object with the provided source and destination
     * ASTs, the provided mappings, and the provided editScript.
     */
    public ASTDiff(TreeContext src, TreeContext dst, MultiMappingStore mappings) {
//        this.srcPath = srcPath;
//        this.dstPath = dstPath;
        this.srcTC = src;
        this.dstTC = dst;
        this.mappings = mappings;
    }
    public void computeEditScript(Map<String, TreeContext> parentContextMap, Map<String, TreeContext> childContextMap)
    {
        this.editScript = new SimplifiedChawatheScriptGenerator().computeActions(this.mappings,parentContextMap,childContextMap);
        processMultiMaps(this.editScript);
    }

    private void processMultiMaps(EditScript editScript) {
//        ArrayList<Action> multiMoves = new ArrayList<>();
        Map<Tree, Set<Tree>> dstToSrcMultis = mappings.dstToSrcMultis();
        MultiMoveActionGenerator multiMoveActionGenerator = new MultiMoveActionGenerator();

        for(Map.Entry<Tree, Set<Tree>> entry : dstToSrcMultis.entrySet())
        {
            Set<Tree> srcTrees = entry.getValue();
            Set<Tree> dstTrees = mappings.getDstForSrc(srcTrees.iterator().next());
            multiMoveActionGenerator.addMapping(srcTrees,dstTrees);
        }
        editScript.addAll(multiMoveActionGenerator.generate());
    }

    /**
     * Compute and return a root node classifier that indicates which node have
     * been added/deleted/updated/moved. Only the root note is marked when a whole
     * subtree has been subject to a same operation.
     */
    public TreeClassifier createRootNodesClassifier() {
        return new OnlyRootsClassifier(this);
    }
}
