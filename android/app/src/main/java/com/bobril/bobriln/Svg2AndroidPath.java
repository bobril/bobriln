package com.bobril.bobriln;

// Heavily inspired by https://github.com/BigBadaboom/androidsvg licensed http://www.apache.org/licenses/LICENSE-2.0
// Optimized to be without allocations, API modified to be independent to XML

import android.graphics.Matrix;
import android.graphics.Path;

public class Svg2AndroidPath {
    private Path path;
    private float[] bezierPoints = new float[6 * 4]; // max 4 segments needed
    private Matrix tempMatrix = new Matrix();
    private String   input;
    private int[]    pos = new int[1];
    private int      inputLength = 0;

    private boolean empty()
    {
        return (pos[0] == inputLength);
    }

    private boolean isWhitespace(int c)
    {
        return (c==' ' || c=='\n' || c=='\r' || c =='\t');
    }

    private void skipWhitespace()
    {
        int p = pos[0];
        while (p < inputLength) {
            if (!isWhitespace(input.charAt(p)))
                break;
            p++;
        }
        pos[0] = p;
    }

    // Skip the sequence: <space>*(<comma><space>)?
    // Returns true if we found a comma in there.
    private boolean skipCommaWhitespace()
    {
        skipWhitespace();
        int p = pos[0];
        if (p == inputLength)
            return false;
        if (!(input.charAt(p) == ','))
            return false;
        pos[0] = p+1;
        skipWhitespace();
        return true;
    }

    private float nextFloat()
    {
        return (float)StringUtils.parseDouble(input, pos);
    }

    /*
     * Scans for a comma-whitespace sequence with a float following it.
     * If found, the float is returned. Otherwise null is returned and
     * the scan position left as it was.
     */
    private float  possibleNextFloat()
    {
        skipCommaWhitespace();
        return nextFloat();
    }

    /*
     * Scans for comma-whitespace sequence with a float following it.
     * But only if the provided 'lastFloat' (representing the last coord
     * scanned was non-null (ie parsed correctly).
     */
    private float  checkedNextFloat(float lastRead)
    {
        if (Float.isNaN(lastRead)) {
            return Float.NaN;
        }
        skipCommaWhitespace();
        return nextFloat();
    }

    private int  nextChar()
    {
        if (pos[0] == inputLength)
            return -1;
        return input.charAt(pos[0]++);
    }
    /*
     * Scan for a 'flag'. A flag is a '0' or '1' digit character.
     */
    private Boolean  nextFlag()
    {
        int p = pos[0];
        if (p == inputLength)
            return null;
        char  ch = input.charAt(p);
        if (ch == '0' || ch == '1') {
            pos[0] = p+1;
            return ch == '1';
        }
        return null;
    }

    /*
     * Like checkedNextFloat, but reads a flag (see path definition parser)
     */
    private Boolean  checkedNextFlag(Object lastRead)
    {
        if (lastRead == null) {
            return null;
        }
        skipCommaWhitespace();
        return nextFlag();
    }

    private boolean  consume(char ch)
    {
        int p = pos[0];
        boolean  found = (p < inputLength && input.charAt(p) == ch);
        if (found)
            pos[0] = p+1;
        return found;
    }

    private boolean  consume(String str)
    {
        int len = str.length();
        int p = pos[0];
        if (p > inputLength - len) return false;
        for (int i=0;i<len;i++) {
            if (input.charAt(p)!=str.charAt(i)) return false;
            p++;
        }
        pos[0] = p;
        return true;
    }

    private int advanceChar()
    {
        int p = pos[0];
        if (p == inputLength)
            return -1;
        p++;
        pos[0] = p;
        if (p < inputLength)
            return input.charAt(p);
        else
            return -1;
    }

    /*
     * Check whether the next character is a letter.
     */
    private boolean  hasLetter()
    {
        int p = pos[0];
        if (p == inputLength)
            return false;
        char  ch = input.charAt(p);
        return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'));
    }

    public boolean parsePath(String val, Path path)
    {
        input = val;
        inputLength = input.length();
        pos[0] = 0;
        this.path = path;

        int     pathCommand = '?';
        float   currentX = 0f, currentY = 0f;    // The last point visited in the subpath
        float   lastMoveX = 0f, lastMoveY = 0f;  // The initial point of current subpath
        float   lastControlX = 0f, lastControlY = 0f;  // Last control point of the just completed bezier curve.
        float   x,y, x1,y1, x2,y2;
        float   rx,ry, xAxisRotation;
        Boolean largeArcFlag, sweepFlag;

        path.rewind();
        if (empty())
            return true;

        pathCommand = nextChar();

        if (pathCommand != 'M' && pathCommand != 'm')
            return false;  // Invalid path - doesn't start with a move

        while (true)
        {
            skipWhitespace();

            switch (pathCommand)
            {
                // Move
                case 'M':
                case 'm':
                    x = nextFloat();
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    // Relative moveto at the start of a path is treated as an absolute moveto.
                    if (pathCommand=='m' && !path.isEmpty()) {
                        x += currentX;
                        y += currentY;
                    }
                    path.moveTo(x, y);
                    currentX = lastMoveX = lastControlX = x;
                    currentY = lastMoveY = lastControlY = y;
                    // Any subsequent coord pairs should be treated as a lineto.
                    pathCommand = (pathCommand=='m') ? 'l' : 'L';
                    break;

                // Line
                case 'L':
                case 'l':
                    x = nextFloat();
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='l') {
                        x += currentX;
                        y += currentY;
                    }
                    path.lineTo(x, y);
                    currentX = lastControlX = x;
                    currentY = lastControlY = y;
                    break;

                // Cubic bezier
                case 'C':
                case 'c':
                    x1 = nextFloat();
                    y1 = checkedNextFloat(x1);
                    x2 = checkedNextFloat(y1);
                    y2 = checkedNextFloat(x2);
                    x = checkedNextFloat(y2);
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='c') {
                        x += currentX;
                        y += currentY;
                        x1 += currentX;
                        y1 += currentY;
                        x2 += currentX;
                        y2 += currentY;
                    }
                    path.cubicTo(x1, y1, x2, y2, x, y);
                    lastControlX = x2;
                    lastControlY = y2;
                    currentX = x;
                    currentY = y;
                    break;

                // Smooth curve (first control point calculated)
                case 'S':
                case 's':
                    x1 = 2 * currentX - lastControlX;
                    y1 = 2 * currentY - lastControlY;
                    x2 = nextFloat();
                    y2 = checkedNextFloat(x2);
                    x = checkedNextFloat(y2);
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='s') {
                        x += currentX;
                        y += currentY;
                        x2 += currentX;
                        y2 += currentY;
                    }
                    path.cubicTo(x1, y1, x2, y2, x, y);
                    lastControlX = x2;
                    lastControlY = y2;
                    currentX = x;
                    currentY = y;
                    break;

                // Close path
                case 'Z':
                case 'z':
                    path.close();
                    currentX = lastControlX = lastMoveX;
                    currentY = lastControlY = lastMoveY;
                    break;

                // Horizontal line
                case 'H':
                case 'h':
                    x = nextFloat();
                    if (Float.isNaN(x)) {
                        return false;
                    }
                    if (pathCommand=='h') {
                        x += currentX;
                    }
                    path.lineTo(x, currentY);
                    currentX = lastControlX = x;
                    break;

                // Vertical line
                case 'V':
                case 'v':
                    y = nextFloat();
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='v') {
                        y += currentY;
                    }
                    path.lineTo(currentX, y);
                    currentY = lastControlY = y;
                    break;

                // Quadratic bezier
                case 'Q':
                case 'q':
                    x1 = nextFloat();
                    y1 = checkedNextFloat(x1);
                    x = checkedNextFloat(y1);
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='q') {
                        x += currentX;
                        y += currentY;
                        x1 += currentX;
                        y1 += currentY;
                    }
                    path.quadTo(x1, y1, x, y);
                    lastControlX = x1;
                    lastControlY = y1;
                    currentX = x;
                    currentY = y;
                    break;

                // Smooth quadratic bezier
                case 'T':
                case 't':
                    x1 = 2 * currentX - lastControlX;
                    y1 = 2 * currentY - lastControlY;
                    x = nextFloat();
                    y = checkedNextFloat(x);
                    if (Float.isNaN(y)) {
                        return false;
                    }
                    if (pathCommand=='t') {
                        x += currentX;
                        y += currentY;
                    }
                    path.quadTo(x1, y1, x, y);
                    lastControlX = x1;
                    lastControlY = y1;
                    currentX = x;
                    currentY = y;
                    break;

                // Arc
                case 'A':
                case 'a':
                    rx = nextFloat();
                    ry = checkedNextFloat(rx);
                    xAxisRotation = checkedNextFloat(ry);
                    largeArcFlag = checkedNextFlag(Float.isNaN(xAxisRotation)? null : true);
                    sweepFlag = checkedNextFlag(largeArcFlag);
                    if (sweepFlag == null)
                        return false;
                    else {
                        x = possibleNextFloat();
                        y = checkedNextFloat(x);
                    }
                    if (Float.isNaN(y) || rx < 0 || ry < 0) {
                        return false;
                    }
                    if (pathCommand=='a') {
                        x += currentX;
                        y += currentY;
                    }
                    arcTo(currentX, currentY, rx, ry, xAxisRotation, largeArcFlag, sweepFlag, x, y);
                    currentX = lastControlX = x;
                    currentY = lastControlY = y;
                    break;

                default:
                    return false;
            }

            skipCommaWhitespace();
            if (empty())
                break;

            // Test to see if there is another set of coords for the current path command
            if (hasLetter()) {
                // Nope, so get the new path command instead
                pathCommand = nextChar();
            }
        }
        return true;
    }

    private void arcTo(float lastX, float lastY, float rx, float ry, float angle, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
        if (lastX == x && lastY == y) {
            // If the endpoints (x, y) and (x0, y0) are identical, then this
            // is equivalent to omitting the elliptical arc segment entirely.
            // (behaviour specified by the spec)
            return;
        }

        // Handle degenerate case (behaviour specified by the spec)
        if (rx == 0 || ry == 0) {
            path.lineTo(x, y);
            return;
        }

        // Sign of the radii is ignored (behaviour specified by the spec)
        rx = Math.abs(rx);
        ry = Math.abs(ry);

        // Convert angle from degrees to radians
        float angleRad = (float) Math.toRadians(angle % 360.0);
        float cosAngle = (float) Math.cos(angleRad);
        float sinAngle = (float) Math.sin(angleRad);

        // We simplify the calculations by transforming the arc so that the origin is at the
        // midpoint calculated above followed by a rotation to line up the coordinate axes
        // with the axes of the ellipse.

        // Compute the midpoint of the line between the current and the end point
        float dx2 = (lastX - x) / 2.0f;
        float dy2 = (lastY - y) / 2.0f;

        // Step 1 : Compute (x1', y1') - the transformed start point
        float x1 = (cosAngle * dx2 + sinAngle * dy2);
        float y1 = (-sinAngle * dx2 + cosAngle * dy2);

        float rx_sq = rx * rx;
        float ry_sq = ry * ry;
        float x1_sq = x1 * x1;
        float y1_sq = y1 * y1;

        // Check that radii are large enough.
        // If they are not, the spec says to scale them up so they are.
        // This is to compensate for potential rounding errors/differences between SVG implementations.
        float radiiCheck = x1_sq / rx_sq + y1_sq / ry_sq;
        if (radiiCheck > 1) {
            rx = (float) Math.sqrt(radiiCheck) * rx;
            ry = (float) Math.sqrt(radiiCheck) * ry;
            rx_sq = rx * rx;
            ry_sq = ry * ry;
        }

        // Step 2 : Compute (cx1, cy1) - the transformed centre point
        float sign = (largeArcFlag == sweepFlag) ? -1 : 1;
        float sq = ((rx_sq * ry_sq) - (rx_sq * y1_sq) - (ry_sq * x1_sq)) / ((rx_sq * y1_sq) + (ry_sq * x1_sq));
        sq = (sq < 0) ? 0 : sq;
        float coef = (float) (sign * Math.sqrt(sq));
        float cx1 = coef * ((rx * y1) / ry);
        float cy1 = coef * -((ry * x1) / rx);

        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        float sx2 = (lastX + x) / 2.0f;
        float sy2 = (lastY + y) / 2.0f;
        float cx = sx2 + (cosAngle * cx1 - sinAngle * cy1);
        float cy = sy2 + (sinAngle * cx1 + cosAngle * cy1);

        // Step 4 : Compute the angleStart (angle1) and the angleExtent (dangle)
        float ux = (x1 - cx1) / rx;
        float uy = (y1 - cy1) / ry;
        float vx = (-x1 - cx1) / rx;
        float vy = (-y1 - cy1) / ry;
        float p, n;

        // Compute the angle start
        n = (float) Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1.0f : 1.0f;
        float angleStart = (float) Math.toDegrees(sign * Math.acos(p / n));

        // Compute the angle extent
        n = (float) Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1.0f : 1.0f;
        double angleExtent = Math.toDegrees(sign * Math.acos(p / n));
        if (!sweepFlag && angleExtent > 0) {
            angleExtent -= 360f;
        } else if (sweepFlag && angleExtent < 0) {
            angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        // Many elliptical arc implementations including the Java2D and Android ones, only
        // support arcs that are axis aligned.  Therefore we need to substitute the arc
        // with bezier curves.  The following method call will generate the beziers for
        // a unit circle that covers the arc angles we want.
        int bezierPointsLength = arcToBeziers(angleStart, angleExtent);

        // Calculate a transformation tempMatrix that will move and scale these bezier points to the correct location.
        Matrix m = tempMatrix;
        m.reset();
        m.postScale(rx, ry);
        m.postRotate(angle);
        m.postTranslate(cx, cy);
        m.mapPoints(bezierPoints, 0, bezierPoints, 0, bezierPointsLength / 2);

        // The last point in the bezier set should match exactly the last coord pair in the arc (ie: x,y). But
        // considering all the mathematical manipulation we have been doing, it is bound to be off by a tiny
        // fraction. Experiments show that it can be up to around 0.00002.  So why don't we just set it to
        // exactly what it ought to be.
        bezierPoints[bezierPointsLength - 2] = x;
        bezierPoints[bezierPointsLength - 1] = y;

        // Final step is to add the bezier curves to the path
        for (int i = 0; i < bezierPointsLength; i += 6) {
            path.cubicTo(bezierPoints[i], bezierPoints[i + 1], bezierPoints[i + 2], bezierPoints[i + 3], bezierPoints[i + 4], bezierPoints[i + 5]);
        }
    }

    /*
     * Generate the control points and endpoints for a set of bezier curves that match
     * a circular arc starting from angle 'angleStart' and sweep the angle 'angleExtent'.
     * The circle the arc follows will be centered on (0,0) and have a radius of 1.0.
     *
     * Each bezier can cover no more than 90 degrees, so the arc will be divided evenly
     * into a maximum of four curves.
     */
    private int arcToBeziers(double angleStart, double angleExtent) {
        int numSegments = (int) Math.ceil(Math.abs(angleExtent) / 90.0);

        angleStart = Math.toRadians(angleStart);
        angleExtent = Math.toRadians(angleExtent);
        double angleIncrement = angleExtent / numSegments;

        double controlLength = 4.0 / 3.0 * Math.sin(angleIncrement / 2.0) / (1.0 + Math.cos(angleIncrement / 2.0));

        int pos = 0;

        double angle = angleStart;
        double dx = Math.cos(angle);
        double dy = Math.sin(angle);

        for (int i = 0; i < numSegments; i++) {
            // First control point
            bezierPoints[pos++] = (float) (dx - controlLength * dy);
            bezierPoints[pos++] = (float) (dy + controlLength * dx);
            // Second control point
            angle += angleIncrement;
            dx = Math.cos(angle);
            dy = Math.sin(angle);
            bezierPoints[pos++] = (float) (dx + controlLength * dy);
            bezierPoints[pos++] = (float) (dy - controlLength * dx);
            // Endpoint of bezier
            bezierPoints[pos++] = (float) dx;
            bezierPoints[pos++] = (float) dy;
        }
        return pos;
    }

    public void parseViewBox(String val, float[] result) {
        input = val;
        inputLength = input.length();
        pos[0] = 0;
        result[0] = nextFloat();
        result[1] = checkedNextFloat(result[0]);
        result[2] = checkedNextFloat(result[1]);
        result[3] = checkedNextFloat(result[2]);
    }
}
