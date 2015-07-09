package domain;

import javafx.scene.input.DataFormat;
import javafx.scene.shape.Rectangle;

import java.io.Serializable;

import arch.IMatrix2D;
import arch.Matrix2D;

//@SuppressWarnings("restriction")
public final class Dominoes implements Serializable {
    private static final long serialVersionUID = 1L;
    public final static DataFormat clipboardFormat = new DataFormat("Domino");

    public final static String DEVICE_GPU = "GPU";
    public final static String DEVICE_CPU = "CPU";

    /*
     * This variables are used to know the sequence of the matrix information in
     * the hour of to save/load in the .TXT format
     */
    public final static int INDEX_TYPE = 0;
    public final static int INDEX_ID_ROW = 1;
    public final static int INDEX_ID_COL = 2;
    public final static int INDEX_HEIGHT = 3;
    public final static int INDEX_WIDTH = 4;
    public final static int INDEX_HIST = 5;
    public final static int INDEX_MATRIX = 6;

    public final static int INDEX_SIZE = 7;

    /*
     * This variables are used to know the type of matrix
     */
    public static enum DominoType {
        BASIC("B"), DERIVED("D"), SUPPORT("S"), CONFIDENCE("C"), LIFT("L");

        private final String code;

        private DominoType(String code) {
            this.code = code;
        };

        public String getCode() {
            return code;
        }
    }

    public final static String AGGREG_TEXT = "/SUM ";

    private boolean rowIsAggragatable = false;
    private boolean colIsAggragatable = false;
    private String idRow;
    private String idCol;
    private Historic historic;
    private DominoType type;
    private IMatrix2D mat = null;
    private String currentDevice = DEVICE_CPU;

    public Dominoes(String _device) {
        currentDevice = _device;
    }

    /**
     * Class Builder This function is used when the user to do a multiplication,
     * this will return a new matrix with data according with a real
     * multiplication. The type, for default, is the type of the first parameter
     *
     * @param firstOperator
     *            The first matrix in this operation
     * @param secondOperator
     *            The second matrix in this operation
     * @param mat
     * @return A new matrix
     */

    /*
     * public Dominoes(Dominoes firstOperator, Dominoes secondOperator, byte[][]
     * mat) throws IllegalArgumentException { this.historic = new ArrayList<>();
     * 
     * this.historic.addAll(firstOperator.getHistoric());
     * this.historic.addAll(this.historic.size(), secondOperator.getHistoric());
     * 
     * this.setIdRow(firstOperator.getIdRow());
     * this.setIdCol(secondOperator.getIdCol());
     * 
     * this.setMat(mat); this.type = firstOperator.getType(); }
     */

    /**
     * Class build. The type, for default, is Basic.
     *
     * @param idRow
     *            - identifier row of the Dominoes matrix
     * @param idCol
     *            - identifier row of the Dominoes matrix
     * @param mat
     *            - matrix2D
     * @throws IllegalArgumentException
     *             - in case of invalid parameters
     */
    public Dominoes(String idRow, String idCol, IMatrix2D mat, String _device) throws IllegalArgumentException {
        this.setIdRow(idRow);
        this.setIdCol(idCol);

        this.setMat(mat);

        this.setHistoric(new Historic(idRow, idCol));

        this.type = DominoType.BASIC;
        this.currentDevice = _device;
    }

    /**
     * Class build. The type, for default, is Derived
     *
     * @param type
     * @param idRow
     *            - identifier row of the Dominoes matrix
     * @param idCol
     *            - identifier row of the Dominoes matrix
     * @param historic
     *            - The dominoes historic derivated
     * @param mat
     *            - matrix2D
     * @throws IllegalArgumentException
     *             - in case of invalid parameters
     */
    public Dominoes(DominoType type, String idRow, String idCol, Historic historic, Matrix2D mat, String _device)
            throws IllegalArgumentException {
        this.setIdRow(idRow);
        this.setIdCol(idCol);

        this.setMat(mat);

        this.setHistoric(historic);
        if (type == DominoType.BASIC) {
            throw new IllegalArgumentException("Invalid argument.\nThe Type attribute is not defined or is not valid");
        }
        this.type = type;
        this.currentDevice = _device;
    }

    // /**
    // * Class build. The type, for default, is Derived
    // *
    // * @param type
    // * @param idRow - identifier row of the Dominoes matrix
    // * @param idCol - identifier row of the Dominoes matrix
    // * @throws IllegalArgumentException - in case of invalid parameters
    // */
    // public Dominoes(int type, String idRow, String idCol) throws
    // IllegalArgumentException {
    // this.setIdRow(idRow);
    // this.setIdCol(idCol);
    //
    // this.historic = new ArrayList<>();
    // this.historic.add(idRow);
    // this.historic.add(idCol);
    //
    // this.type = Dominoes.TYPE_BASIC;
    //
    // this.mat = null;
    // }

    /**
     * From this Dominoes, this function will build a piece (graphically)
     * respective to this dominoes
     *
     * @return - A javafx.scene.Group (Graphic) to draw in scene
     */
    public DominoView drawDominoes() {
        return new DominoView(this);
    }

    /**
     * User to obtain the complete historic this
     *
     * @return this Historic
     */
    public Historic getHistoric() {
        return this.historic;
    }

    /**
     * Used to obtain the Id Column this Domino
     *
     * @return Return the Id Column value
     */
    public String getIdCol() {
        return this.idCol;
    }

    /**
     * Used to obtain the Id Column this Domino
     *
     * @return Return the Id Row value
     */
    public String getIdRow() {
        return this.idRow;
    }

    /**
     * Used to obtain the Matrix this Domino
     *
     * @return Return the Matrix value
     */
    public IMatrix2D getMat() {
        return this.mat;
    }

    /**
     * Used to obtain the Type of Matrix
     *
     * @return Return the Type value
     */
    public DominoType getType() {
        return type;
    }

    public boolean isRowAggregatable() {
        return this.rowIsAggragatable;
    }

    public boolean isColAggregatable() {
        return this.colIsAggragatable;
    }

    /**
     * Used to change the Historic of this Domino
     *
     * @param historic
     *            The Historic value
     * @throws IllegalArgumentException
     */
    private void setHistoric(Historic historic) {
        if (historic == null || historic.toString() == null || historic.toString().trim().equals("")
                || (!historic.getFirstItem().equals(this.idRow) && !historic.getLastItem().equals(this.idCol))) {
            throw new IllegalArgumentException("Invalid argument.\nThe Historic attribute is null, void or invalid");
        }
        this.historic = historic;
    }

    /**
     * Used to change the Id Column this Domino
     *
     * @param idCol
     *            The Id Column value
     * @throws IllegalArgumentException
     */
    private void setIdCol(String idCol) throws IllegalArgumentException {
        if (idCol == null || idCol.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe IdCol attribute is null or void");
        }
        this.idCol = idCol;
    }

    /**
     * Used to change the Id Row this Domino
     *
     * @param idRow
     *            The Id Row value
     * @throws IllegalArgumentException
     */
    private void setIdRow(String idRow) throws IllegalArgumentException {
        if (idRow == null || idRow.trim().equals("")) {
            throw new IllegalArgumentException("Invalid argument.\nThe IdRow attribute is null or void");
        }
        this.idRow = idRow;
    }

    /**
     * Used to change the Matrix this Domino
     *
     * @param mat
     *            The Matrix value
     * @throws IllegalArgumentException
     */
    public void setMat(IMatrix2D mat) {
        if (mat == null) {
            throw new IllegalArgumentException("Invalid argument.\nThe Mat attribute is null");
        }

        this.mat = mat;
    }

    /**
     * This function just invert the Historic
     *
     * @return the historic invert
     */
    public void transpose() {

        if (!(this.type == DominoType.BASIC)) {
            this.type = DominoType.DERIVED;
        }
        if (this.getIdRow().equals(this.getIdCol())) {
            this.type = DominoType.SUPPORT;
        }

        this.getHistoric().reverse();
        this.setIdRow(this.getHistoric().getFirstItem());
        this.setIdCol(this.getHistoric().getLastItem());

        boolean swap = this.rowIsAggragatable;
        this.rowIsAggragatable = this.colIsAggragatable;
        this.colIsAggragatable = swap;

        IMatrix2D _newMat = mat.transpose();
        setMat(_newMat);
    }

    /**
     * This function just invert the Historic
     *
     * @return the historic invert
     */
    public void confidence() {
        IMatrix2D _newMat = mat.confidence(currentDevice.equalsIgnoreCase("GPU"));
        setMat(_newMat);
    }

    /**
     * This function reduce the lines of a matrix
     *
     * @return the historic invert
     */
    public boolean reduceRows() {

        if (rowIsAggragatable) {
            return false;
        }

        rowIsAggragatable = true;

        if (!(this.type == DominoType.BASIC)) {
            this.type = DominoType.DERIVED;
        }
        if (this.getIdRow().equals(this.getIdCol())) {
            this.type = DominoType.SUPPORT;
        }

        // this.getHistoric().reverse();
        this.setIdRow(Dominoes.AGGREG_TEXT + idRow);
        this.historic.reduceRow();
        // this.setIdCol(this.getHistoric().getLastItem());

        // this.historic = new Historic("SUM", this.getIdCol());

        IMatrix2D _newMat = mat.reduceRows(currentDevice.equalsIgnoreCase("GPU"));
        setMat(_newMat);

        _newMat.Debug();
        return true;
    }

    public Dominoes multiply(Dominoes dom) {

        Dominoes domResult = new Dominoes(dom.getDevice());

        domResult.type = DominoType.DERIVED;

        if (idRow.equals(dom.getIdCol())) {
            domResult.type = DominoType.SUPPORT;
        }

        try {
            domResult.setMat(getMat().multiply(dom.getMat(), currentDevice.equalsIgnoreCase("GPU")));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        domResult.historic = new Historic(this.getHistoric(), dom.getHistoric());

        domResult.setIdRow(getIdRow());
        domResult.setIdCol(dom.getIdCol());

        return domResult;
    }

    public boolean isSquare() {
        return getMat().getMatrixDescriptor().getNumRows() == getMat().getMatrixDescriptor().getNumCols();
    }

    public Dominoes cloneNoMatrix() {
        return new Dominoes(getIdRow(), getIdCol(), getMat(), getDevice());
    }

    public String getDevice() {
        return currentDevice;
    }

    @Override
    public String toString() {
        return historic.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Dominoes))
            return false;
        Dominoes o = (Dominoes) obj;

        boolean ret = type.equals(o.type) && o.colIsAggragatable == colIsAggragatable
                && o.rowIsAggragatable == rowIsAggragatable && o.currentDevice.equals(currentDevice)
                && o.historic.equals(historic) && o.idCol.equals(idCol) && o.idRow.equals(idRow) && o.mat.equals(mat);
        // System.out.println(o + " equals " + this + "? " + ret);
        // if (o.toString().equals(this.toString())) {
        // System.out.println(type.equals(o.type));
        // System.out.println(o.colIsAggragatable == colIsAggragatable);
        // System.out.println(o.rowIsAggragatable == rowIsAggragatable);
        // System.out.println(o.currentDevice.equals(currentDevice));
        // System.out.println(o.historic.equals(historic));
        // System.out.println(o.idCol.equals(idCol));
        // System.out.println(o.idRow.equals(idRow));
        // System.out.println(o.mat.equals(mat));
        // }
        return ret;
    }

    // @Override
    // public int hashCode() {
    // return mat.hashCode();
    // }
}
