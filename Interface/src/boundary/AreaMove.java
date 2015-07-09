package boundary;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.text.Font;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import domain.Configuration;
import domain.DominoView;
import domain.Dominoes;

//@SuppressWarnings("restriction")
public class AreaMove extends Pane {

    private ArrayList<Dominoes> dominoes;
    private ArrayList<DominoView> pieces;
    private final Rectangle background;

    private int indexFirstOperatorMultiplication = -1;
    private int indexSecondOperatorMultiplication = -1;    

    private double srcSceneX;
    private double srcSceneY;
    private double srcTranslateX;
    private double srcTranslateY;

    private double padding = Configuration.width;
    
    private BooleanProperty transposing = new SimpleBooleanProperty();

    // Controller Variables
    private List<MenuItem> menuItemAggregateRow = new ArrayList<>();
    private List<MenuItem> menuItemAggregateCol = new ArrayList<>();
    /**
     * Class builder with the dimension defined in parameters. here, will create
     * too a background with white color
     *
     */
    public AreaMove() {
        super();
        this.background = new Rectangle();
        this.background.setFill(new Color(1, 1, 1, 1));

        this.getChildren().addAll(background);

        this.dominoes = new ArrayList<>();
        this.pieces = new ArrayList<>();
        
        background.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard db = event.getDragboard();
            if (db.hasContent(Dominoes.clipboardFormat)) {
                success = true;
                Object content = db.getContent(Dominoes.clipboardFormat);
                if (content instanceof Dominoes) {
                    System.out.println("Drop point is (" + event.getX() + "," + event.getY() + ")");
                    add((Dominoes) content, event.getX() - DominoView.GRAPH_WIDTH/2, event.getY()-DominoView.GRAPH_HEIGHT/2);
                } else {
                    System.out.println("Drop failed..");
                    success = false;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });

//        background.setOnDragDetected(event -> {
//            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
//            ClipboardContent content = new ClipboardContent();
//            content.put(Dominoes.clipboardFormat, domino);
//            dragboard.setDragView(group.snapshot(null, null));
//            dragboard.setDragViewOffsetX(event.getX());
//            dragboard.setDragViewOffsetY(event.getY());
//            dragboard.setContent(content);
//            event.consume();
//        });

        background.setOnDragOver(event -> {
            if (event.getGestureSource() != background && event.getDragboard().hasContent(Dominoes.clipboardFormat)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
    }

    /**
     * Add a new Domino in Area Move in position x = 0, y = 0
     *
     * @param domino The Domino information
     */
    public void add(Dominoes domino) {
        this.add(domino, 0, 0);
    }

    /**
     * Add a new Domino in Area Move in position defined for parameters
     *
     * @param domino The Domino information
     * @param x The coordinate X of this new Domino
     * @param y The coordinate Y of this new Domino
     */
    public void add(Dominoes domino, double x, double y) {
        x = x < 0 ? 0 : x;
        y = y < 0 ? 0 : y;
        
        ContextMenu minimenu = new ContextMenu();
        
        MenuItem menuItemTranspose = new MenuItem("Transpose");

		menuItemAggregateRow.add(new MenuItem("Aggregate by " + domino.getMat().getMatrixDescriptor().getRowType()));
        menuItemAggregateCol.add(new MenuItem("Aggregate by " + domino.getMat().getMatrixDescriptor().getColType()));
        MenuItem menuItemConfidence = new MenuItem("Confidence");
		
		MenuItem menuItemZScore = new MenuItem("Z-Score");
        MenuItem menuItemSaveInList = new MenuItem("Save");
        MenuItem menuItemViewGraph = new MenuItem("Graph");
        MenuItem menuItemViewMatrix = new MenuItem("Matrix");
        MenuItem menuItemViewChart = new MenuItem("Bar Chart");
        MenuItem menuItemViewLineChart = new MenuItem("Line Chart");
        MenuItem menuItemViewTree = new MenuItem("Tree");
        MenuItem menuItemClose = new MenuItem("Close");
        
        
        Menu menuOperate = new Menu("Operations");
        Menu menuView = new Menu("Views");

        DominoView group = domino.drawDominoes();
        group.setVisible(DominoView.Views.GRAPH_HISTORIC,Configuration.visibilityHistoric);

        group.setTranslateX(x);
        group.setTranslateY(y);
        // TODO note the bug here. If two pieces are added, and then you swap their positions,
        //      what happens when you add a third piece? JAH
        for (DominoView piece : pieces) {
            if (group.getBoundsInParent().intersects(piece.getBoundsInParent())) {
                group.setTranslateY(piece.getTranslateY() + DominoView.GRAPH_HEIGHT);
            }
        }

        this.pieces.add(group);
        this.dominoes.add(domino);
        this.getChildren().add(group);

        //if (!domino.getIdRow().equals(domino.getIdCol())) {
          //  menuItemViewGraph.setDisable(true);
        //}

        group.setOnMouseEntered(event -> cursorProperty().set(Cursor.OPEN_HAND));
        group.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - srcSceneX;
            double offsetY = event.getSceneY() - srcSceneY;
            double newTranslateX = srcTranslateX + offsetX;
            double newTranslateY = srcTranslateY + offsetY;

            // detect move out
            boolean detecMoveOutX = false;
            boolean detecMoveOutY = false;
            if (newTranslateX < background.getX()) {
                ((Group) (event.getSource())).setTranslateX(background.getX());

                detecMoveOutX = true;
            }
            if (newTranslateY < background.getY()) {
                ((Group) (event.getSource())).setTranslateY(background.getY());
                detecMoveOutY = true;
            }
            if (newTranslateX + ((Group) (event.getSource())).prefWidth(-1) > background.getX() + background.getWidth()) {
                ((Group) (event.getSource())).setTranslateX(background.getX() + background.getWidth() - ((Group) (event.getSource())).prefWidth(-1));
                detecMoveOutX = true;
            }
            if (newTranslateY + ((Group) (event.getSource())).prefHeight(-1) > background.getY() + background.getHeight()) {
                ((Group) (event.getSource())).setTranslateY(background.getY() + background.getHeight() - ((Group) (event.getSource())).prefHeight(-1));
                detecMoveOutY = true;
            }

            if (!detecMoveOutX) {
                ((Group) (event.getSource())).setTranslateX(newTranslateX);
            }
            if (!detecMoveOutY) {
                ((Group) (event.getSource())).setTranslateY(newTranslateY);
            }

            // detect multiplication
            int index = pieces.indexOf(group);

            for (int j = 0; j < pieces.size(); j++) {

                if (index != j && detectMultiplication(index, j)) {
                    //menuItemReduceLines.setDisable(false);

                    break;
                } else {
                    //menuItemReduceLines.setDisable(true);
                }
            }
        });
        group.setOnMousePressed(event -> {
            srcSceneX = event.getSceneX();
            srcSceneY = event.getSceneY();
            srcTranslateX = ((Group) (event.getSource())).getTranslateX();
            srcTranslateY = ((Group) (event.getSource())).getTranslateY();

            group.toFront();
            cursorProperty().set(Cursor.CLOSED_HAND);
        });
        group.setOnMouseReleased(event -> {
            cursorProperty().set(Cursor.OPEN_HAND);
            try {
                multiply();
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        });
        group.setOnMouseExited(event -> cursorProperty().set(Cursor.DEFAULT));
        group.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {

                if (event.getClickCount() == 2) {
                    try {
                    	if(!transposing.get()){
                    		transpose(group);
                    	}
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        });
        group.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                minimenu.show(group, event.getScreenX(), event.getScreenY());
            } else {
                minimenu.hide();
            }
        });
        minimenu.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });
        minimenu.setOnAction(event -> {
            if (((MenuItem) event.getTarget()).getText().equals(menuItemSaveInList.getText())) {
                System.out.println("saving");
                try {
                    saveAndSendToList(group);
                    close(group);
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else if (((MenuItem) event.getTarget()).getText().equals(menuItemClose.getText())) {
                System.out.println("closing");
                closePiece(group);
            }
        });
        int index = dominoes.indexOf(domino);
        menuOperate.setOnAction(event -> {
            try {
                String text = ((MenuItem) event.getTarget()).getText();
                if (text.equals(menuItemTranspose.getText())) {
                	if(!transposing.get()){
                		System.out.println("transposing");
                    	transpose(group);
                	}
                } else if (text.equals(menuItemAggregateRow.get(index).getText())) {
                    reduceColumns(group);
                } else if (text.equals(menuItemAggregateCol.get(index).getText())) {
                    reduceLines(group);
                } else if (text.equals(menuItemConfidence.getText())) {
					confidence(group);
				}
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        });
        menuView.setOnAction(event -> {
            String text = ((MenuItem) event.getTarget()).getText();
            if (text.equals(menuItemViewGraph.getText())) {
                drawGraph(domino);
            } else if (text.equals(menuItemViewMatrix.getText())) {
                drawMatrix(domino);
            } else if (text.equals(menuItemViewChart.getText())) {
                drawChart(domino);
            } else if (text.equals(menuItemViewTree.getText())) {
                drawTree(domino);
            } else if (text.equals(menuItemViewLineChart.getText())) {
    			drawLineChart(domino);
    		}
        });
        

        menuOperate.getItems().addAll(menuItemTranspose, menuItemAggregateRow.get(index),
        		menuItemAggregateCol.get(index), menuItemConfidence, menuItemZScore);
        menuView.getItems().addAll(menuItemViewChart, /*menuItemViewLineChart,*/ 
        		menuItemViewGraph, menuItemViewMatrix/*, menuItemViewTree*/);
        minimenu.getItems().addAll(menuOperate, menuView, menuItemSaveInList, menuItemClose);
    }
    
    /**
     * This function remove all parts in this area move
     */
    public void clear() {
    	if(this.pieces == null || this.dominoes == null) return;
        for (int i = 0; i < this.pieces.size(); i++) {
            this.pieces.get(i).setVisible(false);
        }
        this.pieces.removeAll(this.pieces);
        this.dominoes.removeAll(this.dominoes);

        this.pieces = null;
        this.dominoes = null;
    }

    /**
     * This function is used to remove a element of this Area Move
     *
     * @param group A specified element
     */
    private void close(Group group) {
        // removing in area move
        this.remove(group);
    }

    /**
     * Just close the piece defined in the parameter
     *
     * @param group The piece to will be removed
     */
    private void closePiece(Group group) {
        remove(group);
    }

    /**
     * To detect a multiplication will be used the interception between the
     * pieces, detecting by left or detecting by right has different
     * significates. All detections are ever in relation to index1 (left or
     * right)
     *
     * @param index1 - piece index one
     * @param index2 - piece index two
     */
    private boolean detectMultiplication(int index1, int index2) {

        DominoView g1 = this.pieces.get(index1);
        DominoView g2 = this.pieces.get(index2);
        Dominoes d1 = this.dominoes.get(index1);
        Dominoes d2 = this.dominoes.get(index2);
        
        if (g1.getBoundsInParent().intersects(g2.getBoundsInParent())) {
            if (g1.getTranslateX() < g2.getTranslateX()) {
                if (d1.getIdCol().equals(d2.getIdRow())
                        && !d1.getIdCol().contains(Dominoes.AGGREG_TEXT)
                        && d1.getMat().getMatrixDescriptor().getNumCols() == d2.getMat().getMatrixDescriptor().getNumRows()) {
                    g1.colorOperateCol();
                    g2.colorOperateRow();
                    
//                    g1.setTranslateX(g2.getTranslateX() - DominoView.GRAPH_WIDTH + paddingToCoupling);
//                    g1.setTranslateY(g2.getTranslateY());

                    this.indexFirstOperatorMultiplication = index1;
                    this.indexSecondOperatorMultiplication = index2;

                    return true;
                }
            } else {
                if (d1.getIdRow().equals(d2.getIdCol())
                        && !d1.getIdRow().contains(Dominoes.AGGREG_TEXT)
                        && d1.getMat().getMatrixDescriptor().getNumRows() == d2.getMat().getMatrixDescriptor().getNumCols() ) {
                    g1.colorOperateRow();
                    g2.colorOperateCol();
                    
//                    g1.setTranslateX(g2.getTranslateX() + DominoView.GRAPH_WIDTH - paddingToCoupling);
//                    g1.setTranslateY(g2.getTranslateY());

                    this.indexFirstOperatorMultiplication = index2;
                    this.indexSecondOperatorMultiplication = index1;

                    return true;
                }
            }
        }
        g1.colorNormalRow();
        g1.colorNormalCol();
        g2.colorNormalRow();
        g2.colorNormalCol();

        this.indexFirstOperatorMultiplication = -1;
        this.indexSecondOperatorMultiplication = -1;

        return false;
    }

    /**
     * This will make a multiplication
     */
    private void multiply() throws IOException {

        if (this.indexFirstOperatorMultiplication != -1 && this.indexSecondOperatorMultiplication != -1) {

            Dominoes d1 = this.dominoes.get(this.indexFirstOperatorMultiplication);
            Dominoes d2 = this.dominoes.get(this.indexSecondOperatorMultiplication);

            if (d1.getIdCol().equals(d2.getIdRow())) {

                Dominoes resultOperation = control.Controller.MultiplyMatrices(d1, d2);

                double x = (this.pieces.get(this.dominoes.indexOf(d1)).getTranslateX()
                        + this.pieces.get(this.dominoes.indexOf(d2)).getTranslateX()) / 2;

                double y = (this.pieces.get(this.dominoes.indexOf(d1)).getTranslateY()
                        + this.pieces.get(this.dominoes.indexOf(d2)).getTranslateY()) / 2;

                if (this.remove(this.indexFirstOperatorMultiplication)
                        && this.indexSecondOperatorMultiplication > this.indexFirstOperatorMultiplication) {
                    this.remove(this.indexSecondOperatorMultiplication - 1);
                } else {
                    this.remove(this.indexSecondOperatorMultiplication);
                }
                
                this.add(resultOperation, x, y);
                if (Configuration.autoSave) {
                    this.saveAndSendToList(pieces.get(dominoes.indexOf(resultOperation)));
                }
            }
            this.indexFirstOperatorMultiplication = -1;
            this.indexSecondOperatorMultiplication = -1;
        }

    }

    /**
     * This function remove the matrix, in the piece and dominoes array, by the
     * element
     *
     * @param group the element to remove
     * @return True in affirmative case
     */
    public boolean remove(Group group) {
        return remove(pieces.indexOf(group));

    }

    /**
     * This function remove the matrix, in the piece and dominoes array, by the
     * index
     *
     * @param index the index to remove
     * @return True in affirmative case
     */
    public boolean remove(int index) {
        if (index > -1) {
            this.pieces.get(index).setVisible(false);
            this.dominoes.remove(index);
            this.pieces.remove(index);
            this.menuItemAggregateRow.remove(index);
            this.menuItemAggregateCol.remove(index);
        }
        return true;
    }

    /**
     * This function save all piece in AreaMove, remove and create a new matrix
     * in the List
     *
     * @throws IOException
     */
    public void saveAllAndSendToList() throws IOException {
        for (int i = 0; i < this.dominoes.size(); i++) {

            control.Controller.saveMatrix(this.dominoes.get(i));

            // adding in list
            App.CopyToList(this.dominoes.get(i));

            this.pieces.get(i).setVisible(false);
        }
        this.dominoes.removeAll(this.dominoes);
        this.pieces.removeAll(this.pieces);

    }

    /**
     * This function save, remove and create a new matrix in the List
     *
     * @param group The matrix which will suffer with this operation
     * @throws IOException
     */
    private void saveAndSendToList(Group group) throws IOException {
        control.Controller.saveMatrix(this.dominoes.get(this.pieces.indexOf(group)));

        // adding in list
        App.CopyToList(this.dominoes.get(this.pieces.indexOf(group)));
    }

    /**
     * This Functions is used to define the moving area size
     *
     * @param width
     * @param height
     */
    public void setSize(double width, double height) {

        this.background.setWidth(width + padding);
        this.background.setHeight(height);

        this.setMinWidth(width - padding);
        this.setPrefWidth(width);
        this.setMaxWidth(width + padding);
        this.setPrefHeight(height);
    }

    /**
     * This function is used to define the visibility of historic
     *
     * @param visibility True to define visible the historic
     */
    void setVisibleHistoric() {
    	pieces.forEach(dominoView -> dominoView.setVisible(DominoView.Views.GRAPH_HISTORIC, Configuration.visibilityHistoric));
    }
    
    /**
     * This function is used to define the visibility of type
     *
     * @param visibility True to define visible the type
     */
    void setVisibleType() {
    	pieces.forEach(dominoView -> dominoView.setVisible(DominoView.Views.GRAPH_TYPE,Configuration.visibilityType));
        
    }

    /**
     * This function makes a simple animation to tranpose a matrix
     *
     * @param piece The piece to animate
     */
    private void transpose(DominoView piece) throws IOException {
    	int index = pieces.indexOf(piece); 
        MenuItem swapMenu = menuItemAggregateRow.get(index);
        menuItemAggregateRow.set(index, menuItemAggregateCol.get(index));
        menuItemAggregateCol.set(index, swapMenu);
        
        Dominoes domino = control.Controller.tranposeDominoes(this.dominoes.get(index));
        DominoView swap = domino.drawDominoes();
        
        piece.animateTranspose(swap,transposing);
    }
    
    /**
     * This function is responsible for summing up all lines in a column
     *
     * @param piece The piece to animate
     */
    private void reduceLines(DominoView piece) throws IOException {
        
    	int index = this.pieces.indexOf(piece);
    	Dominoes toReduce = this.dominoes.get(index);
    	if(!toReduce.isRowAggregatable()){
    		Dominoes domino = control.Controller.reduceDominoes(toReduce);
    		this.dominoes.set(index, domino);
    		
    		piece.setRowText(domino.getIdRow(),new Font(DominoView.GRAPH_AGGREG_FONT_SIZE));
    		
    		if (Configuration.autoSave) {
    			this.saveAndSendToList(piece);
    		}
    		
    		this.menuItemAggregateRow.get(index).setDisable(true);
    		
    	}else{
    		System.err.println("this domino is already aggregate by " + toReduce.getMat().getMatrixDescriptor().getRowType());
    	}

    }
    
    /**
     * This function is responsible for summing up all columns in a line
     *
     * @param piece The piece to animate
     */
    private void reduceColumns(DominoView piece) throws IOException {
    	
        int index = this.pieces.indexOf(piece);
        Dominoes toReduce = this.dominoes.get(index);

        if(!toReduce.isColAggregatable()){
        	toReduce.transpose();
        	Dominoes domino = control.Controller.reduceDominoes(toReduce);
        	domino.transpose();
        	this.dominoes.set(index, domino);
        	
            piece.setColText(domino.getIdCol(),new Font(DominoView.GRAPH_AGGREG_FONT_SIZE));
            
        	if (Configuration.autoSave) {
        		this.saveAndSendToList(piece);
        	}
        	
        	this.menuItemAggregateCol.get(index).setDisable(true);
    	}else{
    		System.err.println("this domino is already aggregate by " + toReduce.getMat().getMatrixDescriptor().getColType());
    	}

    }
    
    /**
     * This function is responsible for calculating the confidence on a matrix
     *
     * @param piece The piece to animate
     * @throws IOException 
     */
    private void confidence(Group piece) throws IOException {
        int index = this.pieces.indexOf(piece);
        Dominoes toConfidence = this.dominoes.get(index);
        Dominoes domino = control.Controller.confidence(toConfidence);
        this.dominoes.set(index, domino);  
       
        if (Configuration.autoSave) {
            this.saveAndSendToList(piece);
        }

    }

    private void drawGraph(Dominoes domino) {
        App.drawGraph(domino);
    }

    private void drawMatrix(Dominoes domino) {
        App.drawMatrix(domino);
    }
    
    private void drawChart(Dominoes domino) {
        App.drawChart(domino);
    }
    
    private void drawLineChart(Dominoes domino) {
        App.drawLineChart(domino);
    }
    
    private void drawTree(Dominoes domino) {
        App.drawTree(domino);
    }
    
    /**
     * This function is called to change the parts color
     */
    void changeColor() {
        this.pieces.forEach(dominoView -> dominoView.changeColor());
    }
}
