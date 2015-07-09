package arch;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.la4j.matrix.SparseMatrix;
import org.la4j.matrix.functor.MatrixProcedure;
import org.la4j.matrix.sparse.CCSMatrix;
import org.la4j.matrix.sparse.CRSMatrix;

public class Matrix2DJava implements IMatrix2D, Serializable {	
    private static final long serialVersionUID = 1L;
    
    private transient SparseMatrix data;
    
    private MatrixDescriptor matrixDescriptor;
	
	public MatrixDescriptor getMatrixDescriptor() {
		return matrixDescriptor;
	}

	public Matrix2DJava(MatrixDescriptor _matrixDescriptor){
		
		matrixDescriptor = _matrixDescriptor;
		
		data = new CRSMatrix(matrixDescriptor.getNumRows(), 
				matrixDescriptor.getNumCols());
	}
	
	public void finalize(){
	}
	
	
	/*public float getElement(String row, String col){
		
		int _colIndex = matrixDescriptor.getColElementIndex(col);
		
		float[] rowData = getRow(row);
		return rowData[_colIndex];
	}*/
	
	public IMatrix2D multiply(IMatrix2D other, boolean useGPU) throws Exception{
		MatrixDescriptor otherDescriptor = other.getMatrixDescriptor();
		
		if (matrixDescriptor.getNumCols() != otherDescriptor.getNumRows())
			throw new Exception("Matrix cannot be multiplied!");
		
		MatrixDescriptor resultDesc = new MatrixDescriptor(
				matrixDescriptor.getRowType(), 
				otherDescriptor.getColType());
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++)
			resultDesc.AddRowDesc(
					matrixDescriptor.getRowAt(i));
		
		for (int i = 0; i < otherDescriptor.getNumCols(); i++)
			resultDesc.AddColDesc(
					otherDescriptor.getColumnAt(i));
		
		Matrix2DJava result = new Matrix2DJava(resultDesc);
		
		Matrix2DJava otherJava = (Matrix2DJava)other; 
		
		result.data = (SparseMatrix) data.multiply(otherJava.data);
		
		
		return result;
	}
	
	public Matrix2DJava transpose(){
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddRowDesc(this.matrixDescriptor.getColumnAt(i));
		
		for (int i = 0; i < this.matrixDescriptor.getNumRows(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getRowAt(i));
		
		Matrix2DJava transpose = new Matrix2DJava(_newDescriptor);
		transpose.data = (SparseMatrix) data.transpose();
		
		return transpose;
	}
	
	
	public void Debug(){
		
		ArrayList<Cell> cells = getNonZeroData();
		
		int currentLine = -1;
		
		for (int i = 0; i < cells.size(); i++){
			Cell cell = cells.get(i);
			
			if (currentLine != cell.row ){
				System.out.println();
				currentLine = cell.row;
			}
			
			System.out.print(cell.value + "\t");
		}
	}
	
	public void ExportCSV(String filename){
		
		StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
	/*	for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}
		
		File f = new File(filename);
		try {
			f.createNewFile();
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(out.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/
	}
	
	public StringBuffer ExportCSV(){
		
		StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		/*for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}*/
		
		return out;
	}
	
	
	
	public float findMinValue(){
		
		return (float) data.min();
	}
	
	public float findMaxValue(){
		
		return (float) data.max();
	}

	@Override
	public void setData(ArrayList<Cell> cells) {
		for (Cell cell : cells){
			data.set(cell.row, cell.col, cell.value);;
		}
		
	}

	@Override
	public ArrayList<Cell> getNonZeroData() {
		
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		data.eachNonZero(new MatrixProcedure() {
			
			@Override
			public void apply(int row, int col, double value) {
				Cell cell = new Cell();
				cell.row = row;
				cell.col = col;
				cell.value = (float)value;
				cells.add(cell);
			}
		});
		
		return cells;
	}

	@Override
	public IMatrix2D reduceRows(boolean useGPU) {
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		

		_newDescriptor.AddRowDesc("SUM");
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getColumnAt(i));
		
		Matrix2DJava reduced = new Matrix2DJava(_newDescriptor);
		
		float []rowSum = new float[this.matrixDescriptor.getNumCols()];
		ArrayList<Cell> nz = getNonZeroData();
		
		for (Cell c : nz){
			rowSum[c.col] += c.value;
		}
		
		ArrayList<Cell> resCells = new ArrayList<Cell>();
		
		for (int i = 0; i < rowSum.length; i++){
			if (Math.abs(rowSum[i]) > 0){
				resCells.add(new Cell(0, i, rowSum[i]));
			}
		}
		reduced.setData(resCells);
		
		return reduced;
	}

	@Override
	public IMatrix2D confidence(boolean useGPU) {
		List<Cell> nonZeros = getNonZeroData();
		
		ArrayList<Cell> newValues = new ArrayList<Cell>();
		
		for (Cell cell : nonZeros){
			Cell c = new Cell();
			c.row = cell.row;
			c.col = cell.col;
			
			float diagonal = (float) data.get(c.row, c.row);
			
			if (diagonal > 0)
				c.value = cell.value / diagonal;
			
			newValues.add(c);
		}
		
		Matrix2DJava confidenceM = new Matrix2DJava(getMatrixDescriptor());
		confidenceM.setData(newValues);
		
		return confidenceM;
	}
	
	public SparseMatrix getMatrix() {
	    return data;
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Matrix2DJava))
            return false;
        Matrix2DJava o = (Matrix2DJava) obj;
        // good story here -- la4j sparse matrix serialization had a bug before version 5.0
        // the print statements below helped track that
//        System.out.println("Rows equal? " + data.equals(o.data));
//        System.out.println(data.cardinality() + " and " + data.rows() + " x " + data.columns());
//        System.out.println(o.data.cardinality() + " and " + o.data.rows() + " x " + o.data.columns());
//        System.out.println(data.getColumn(0));
//        System.out.println(o.data.getColumn(0));
//        System.out.println(data.sum());
//        System.out.println(o.data.sum());
//        System.out.println(data.subtract(o.data).sum());
//        Debug();
//        System.out.println("Other:");
//        o.Debug();
        return data.equals(o.data);
	}
	
//	@Override
//	public int hashCode() {
//	    
//	}
    
    /**
    * Custom deserialization is needed for the matrix.
    */
    private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
      aStream.defaultReadObject();
      byte[] bin = (byte[]) aStream.readObject();
      try { // transpose annoyingly switches between matrix types in newer la4j implementations
          data = CRSMatrix.fromBinary(bin);
      } catch (IllegalArgumentException e) {
          data = CCSMatrix.fromBinary(bin);
      }
    }

    /**
    * Custom serialization is needed for the matrix.
    */
    private void writeObject(ObjectOutputStream aStream) throws IOException {
      aStream.defaultWriteObject();
      aStream.writeObject(data.toBinary());
    }
}