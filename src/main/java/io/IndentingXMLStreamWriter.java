package io;



import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class IndentingXMLStreamWriter extends StreamWriterDelegate implements Indentation {

    public IndentingXMLStreamWriter(XMLStreamWriter out) {
        super(out);
    }

    /** How deeply nested the current scope is. The root element is depth 1. */
    private int depth = 0; // document scope

    /** stack[depth] indicates what's been written into the current scope. */
    private int[] stack = new int[] { 0, 0, 0, 0 }; // nothing written yet

    private static final int WROTE_MARKUP = 1;

    private static final int WROTE_DATA = 2;

    private String indent = DEFAULT_INDENT;

    private String newLine = NORMAL_END_OF_LINE;

    /** newLine followed by copies of indent. */
    private char[] linePrefix = null;

    @Override
    public void setIndent(String indent) {
        if (!indent.equals(this.indent)) {
            this.indent = indent;
            linePrefix = null;
        }
    }

    @Override
    public String getIndent() {
        return indent;
    }

    @Override
    public void setNewLine(String newLine) {
        if (!newLine.equals(this.newLine)) {
            this.newLine = newLine;
            linePrefix = null;
        }
    }

    /**
     * @return System.getProperty("line.separator"); or
     *         {@link #NORMAL_END_OF_LINE} if that fails.
     */
    public static String getLineSeparator() {
        try {
            return System.getProperty("line.separator");
        } catch (SecurityException ignored) {
            ignored.printStackTrace();
        }
        return NORMAL_END_OF_LINE;
    }

    @Override
    public String getNewLine() {
        return newLine;
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument();
        afterMarkup();
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument(version);
        afterMarkup();
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        beforeMarkup();
        out.writeStartDocument(encoding, version);
        afterMarkup();
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        beforeMarkup();
        out.writeDTD(dtd);
        afterMarkup();
    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        beforeMarkup();
        out.writeProcessingInstruction(target);
        afterMarkup();
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        beforeMarkup();
        out.writeProcessingInstruction(target, data);
        afterMarkup();
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        beforeMarkup();
        out.writeComment(data);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(localName);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(namespaceURI, localName);
        afterMarkup();
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        beforeMarkup();
        out.writeEmptyElement(prefix, localName, namespaceURI);
        afterMarkup();
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(localName);
        afterStartElement();
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(namespaceURI, localName);
        afterStartElement();
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI)
            throws XMLStreamException {
        beforeStartElement();
        out.writeStartElement(prefix, localName, namespaceURI);
        afterStartElement();
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        out.writeCharacters(text);
        afterData();
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        out.writeCharacters(text, start, len);
        afterData();
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        out.writeCData(data);
        afterData();
    }

    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        out.writeEntityRef(name);
        afterData();
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        beforeEndElement();
        out.writeEndElement();
        afterEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        try {
            while (depth > 0) {
                writeEndElement(); // indented
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        out.writeEndDocument();
        afterEndDocument();
    }

    /** Prepare to write markup, by writing a new line and indentation. */
    protected void beforeMarkup() {
        int soFar = stack[depth];
        if ((soFar & WROTE_DATA) == 0 // no data in this scope
                && (depth > 0 || soFar != 0)) {
            try {
                writeNewLine(depth);
                if (depth > 0 && getIndent().length() > 0) {
                    afterMarkup(); // indentation was written
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** Note that markup or indentation was written. */
    protected void afterMarkup() {
        stack[depth] |= WROTE_MARKUP;
    }

    /** Note that data were written. */
    protected void afterData() {
        stack[depth] |= WROTE_DATA;
    }

    /** Prepare to start an element, by allocating stack space. */
    protected void beforeStartElement() {
        beforeMarkup();
        if (stack.length <= depth + 1) {
            // Allocate more space for the stack:
            int[] newStack = new int[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[depth + 1] = 0; // nothing written yet
    }

    /** Note that an element was started. */
    protected void afterStartElement() {
        afterMarkup();
        ++depth;
    }

    /** Prepare to end an element, by writing a new line and indentation. */
    protected void beforeEndElement() {
        if (depth > 0 && stack[depth] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(depth - 1);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    /** Note that an element was ended. */
    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
        }
    }

    /** Note that a document was ended. */
    protected void afterEndDocument() {
        if (stack[depth = 0] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(0);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        stack[depth] = 0; // start fresh
    }

    /** Write a line separator followed by indentation. */
    protected void writeNewLine(int indentation) throws XMLStreamException {
        final int newLineLength = getNewLine().length();
        final int prefixLength = newLineLength + (getIndent().length() * indentation);
        if (prefixLength > 0) {
            if (linePrefix == null) {
                linePrefix = (getNewLine() + getIndent()).toCharArray();
            }
            while (prefixLength > linePrefix.length) {
                // make linePrefix longer:
                char[] newPrefix = new char[newLineLength
                        + ((linePrefix.length - newLineLength) * 2)];
                System.arraycopy(linePrefix, 0, newPrefix, 0, linePrefix.length);
                System.arraycopy(linePrefix, newLineLength, newPrefix, linePrefix.length,
                        linePrefix.length - newLineLength);
                linePrefix = newPrefix;
            }
            out.writeCharacters(linePrefix, 0, prefixLength);
        }
    }

}
