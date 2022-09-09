

package view.webdiff;

import actions.ASTDiff;
import actions.TreeClassifier;
import actions.model.MultiMove;
import utils.SequenceAlgorithms;
import tree.Tree;
import tree.TreeContext;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VanillaDiffHtmlBuilder {

    private static final String SRC_MV_SPAN = "<span class=\"%s\" id=\"move-src-%d\" data-title=\"%s\">";
    private static final String DST_MV_SPAN = "<span class=\"%s\" id=\"move-dst-%d\" data-title=\"%s\">";
    private static final String ADD_DEL_SPAN = "<span class=\"%s\" data-title=\"%s\">";

    private static final String MoveIn_SPAN = "<span class=\"%s\" data-toggle=\"tooltip\" title=\"%s\">";

    private static final String MoveOut_SPAN = "<span class=\"%s\" data-toggle=\"tooltip\" title=\"%s\">";

    private static final String MM_SPAN = "<span class=\"%s\" gid=\"%x\" data-title=\"%s\">";

    private static final String UPD_SPAN = "<span class=\"cupd\">";
    private static final String ID_SPAN = "<span class=\"marker\" id=\"mapping-%d\"></span>";
    private static final String END_SPAN = "</span>";

    private String srcDiff;

    private String dstDiff;

    private ArrayList<Tree> srcMM = new ArrayList<>();
    private ArrayList<Tree> dstMM = new ArrayList<>();

    private String srcContent;

    private String dstContent;

    private ASTDiff diff;

    public VanillaDiffHtmlBuilder(String srcContent, String dstContent, ASTDiff diff) {
        this.srcContent = srcContent;
        this.dstContent = dstContent;
        this.diff = diff;
    }

    public void produce() throws IOException {
        TreeClassifier c = diff.createRootNodesClassifier();
//        Object2IntMap<Tree> mappingIds = new Object2IntOpenHashMap<>();

        int uId = 1;
        int mId = 1;

        TagIndex ltags = new TagIndex();
        for (Tree t: diff.getSrcTC().getRoot().preOrder()) {
            if (c.getMovedSrcs().containsKey(t)) {
//                mappingIds.put(diff.getMappings().getDstForSrc(t).iterator().next(), mId);
                ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                ltags.addTags(t.getPos(), String.format(
                                SRC_MV_SPAN, "token mv", c.getMovedSrcs().get(t), tooltip(diff.getSrcTC(), t)), t.getEndPos(), END_SPAN);
            }
            if (c.getUpdatedSrcs().contains(t)) {
//                mappingIds.put(diff.getMappings().getDstForSrc(t).iterator().next(), mId);
                ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                ltags.addTags(t.getPos(), String.format(
                                SRC_MV_SPAN, "token upd", mId++, tooltip(diff.getSrcTC(), t)), t.getEndPos(), END_SPAN);
                List<int[]> hunks = SequenceAlgorithms.hunks(t.getLabel(), diff.getMappings().getDstForSrc(t).iterator().next().getLabel());
                for (int[] hunk: hunks)
                    ltags.addTags(t.getPos() + hunk[0], UPD_SPAN, t.getPos() + hunk[1], END_SPAN);

            }
            if (c.getDeletedSrcs().contains(t)) {
                ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                ltags.addTags(t.getPos(), String.format(
                                ADD_DEL_SPAN, "token del", tooltip(diff.getSrcTC(), t)), t.getEndPos(), END_SPAN);
            }
            if (c.getMultiMapSrc().containsKey(t)) {
                if (!srcMM.contains(t)) {
                    int gid = ((MultiMove) (c.getMultiMapSrc().get(t))).getGroupId();
                    ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                    boolean updated = ((MultiMove) (c.getMultiMapSrc().get(t))).isUpdated();
                    String htmlClass = "token mm";
                    if (updated) htmlClass += " updated";
                    ltags.addTags(t.getPos(), String.format(
                        MM_SPAN, htmlClass, gid,  tooltip(diff.getSrcTC(), t)), t.getEndPos(), END_SPAN);
                    srcMM.add(t);
                }
            }
            if (c.getSrcMoveOutTreeMap().containsKey(t))
            {
                ltags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                ltags.addTags(t.getPos(), String.format(
                        MoveOut_SPAN, "token moveOut",c.getSrcMoveOutTreeMap().get(t).toString()), t.getEndPos(), END_SPAN);
            }
        }

        TagIndex rtags = new TagIndex();
        for (Tree t: diff.getDstTC().getRoot().preOrder()) {
            if (c.getMovedDsts().containsKey(t)) {
//                int dId = mappingIds.getInt(t);

                rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                rtags.addTags(t.getPos(), String.format(
                                DST_MV_SPAN, "token mv", c.getMovedDsts().get(t), tooltip(diff.getDstTC(), t)), t.getEndPos(), END_SPAN);
            }
            if (c.getUpdatedDsts().contains(t)) {
//                int dId = mappingIds.getInt(t);
                int dId = 0;
                rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                rtags.addTags(t.getPos(), String.format(
                                DST_MV_SPAN, "token upd", dId, tooltip(diff.getDstTC(), t)), t.getEndPos(), END_SPAN);
                List<int[]> hunks = SequenceAlgorithms.hunks(diff.getMappings().getSrcForDst(t).iterator().next().getLabel(), t.getLabel());
                for (int[] hunk: hunks)
                    rtags.addTags(t.getPos() + hunk[2], UPD_SPAN, t.getPos() + hunk[3], END_SPAN);
            }
            if (c.getInsertedDsts().contains(t)) {
                rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                rtags.addTags(t.getPos(), String.format(
                                ADD_DEL_SPAN, "token add", tooltip(diff.getDstTC(), t)), t.getEndPos(), END_SPAN);
            }
            if (c.getMultiMapDst().containsKey(t)) {
                if (!dstMM.contains(t)) {
                    int gid = ((MultiMove) (c.getMultiMapDst().get(t))).getGroupId();
                    boolean updated = ((MultiMove) (c.getMultiMapDst().get(t))).isUpdated();
                    rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                    String htmlClass = "token mm";
                    if (updated) htmlClass += " updated";
                    rtags.addTags(t.getPos(), String.format(
                            MM_SPAN, htmlClass, gid,  tooltip(diff.getDstTC(), t)), t.getEndPos(), END_SPAN);
                    dstMM.add(t);
                }
            }
            if (c.getDstMoveInTreeMap().containsKey(t))
            {
                rtags.addStartTag(t.getPos(), String.format(ID_SPAN, uId++));
                rtags.addTags(t.getPos(), String.format(
                        MoveIn_SPAN, "token moveIn",c.getDstMoveInTreeMap().get(t).toString()), t.getEndPos(), END_SPAN);
            }
        }

        StringWriter w1 = new StringWriter();
//        BufferedReader r = Files.newBufferedReader(fSrc.toPath(), Charset.forName("UTF-8"));
        Reader inputString = new StringReader(srcContent);
        BufferedReader r = new BufferedReader(inputString);
        int cursor = 0;
        for (char cr : srcContent.toCharArray())
        {
            w1.append(ltags.getEndTags(cursor));
            w1.append(ltags.getStartTags(cursor));
            append(cr, w1);
            cursor++;
        }
        w1.append(ltags.getEndTags(cursor));
        r.close();
        srcDiff = w1.toString();

        StringWriter w2 = new StringWriter();
//        r = Files.newBufferedReader(fDst.toPath(), Charset.forName("UTF-8"));
        inputString = new StringReader(dstContent);
        r = new BufferedReader(inputString);
        cursor = 0;

        for (char cr : dstContent.toCharArray())
        {
            w2.append(rtags.getEndTags(cursor));
            w2.append(rtags.getStartTags(cursor));
            append(cr, w2);
            cursor++;
        }
        w2.append(rtags.getEndTags(cursor));
        r.close();

        dstDiff = w2.toString();
    }

    public String getSrcDiff() {
        return srcDiff;
    }

    public String getDstDiff() {
        return dstDiff;
    }

    private static String tooltip(TreeContext ctx, Tree t) {
        return (t.getParent() != null)
                ? t.getParent().getType() + "/" + t.getType() : t.getType().toString();
    }

    private static void append(char cr, Writer w) throws IOException {
        if (cr == '<') w.append("&lt;");
        else if (cr == '>') w.append("&gt;");
        else if (cr == '&') w.append("&amp;");
        else w.append(cr);
    }

    private static class TagIndex {

        private Map<Integer, List<String>> startTags;

        private Map<Integer, List<String>> endTags;

        public TagIndex() {
            startTags = new HashMap<Integer, List<String>>();
            endTags = new HashMap<Integer, List<String>>();
        }

        public void addTags(int pos, String startTag, int endPos, String endTag) {
            addStartTag(pos, startTag);
            addEndTag(endPos, endTag);
        }

        public void addStartTag(int pos, String tag) {
            if (!startTags.containsKey(pos)) startTags.put(pos, new ArrayList<String>());
            startTags.get(pos).add(tag);
        }

        public void addEndTag(int pos, String tag) {
            if (!endTags.containsKey(pos)) endTags.put(pos, new ArrayList<String>());
            endTags.get(pos).add(tag);
        }

        public String getEndTags(int pos) {
            if (!endTags.containsKey(pos)) return "";
            StringBuilder b = new StringBuilder();
            for (String s: endTags.get(pos)) b.append(s);
            return b.toString();
        }

        public String getStartTags(int pos) {
            if (!startTags.containsKey(pos))
                return "";
            StringBuilder b = new StringBuilder();
            for (String s: startTags.get(pos))
                b.append(s);
            return b.toString();
        }

    }
}
