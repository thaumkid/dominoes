package boundary;

/*
 * drag and drop adapted from https://gist.github.com/jewelsea/7821196
 */

import domain.Configuration;
import domain.DominoView;
import domain.Dominoes;

import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

//@SuppressWarnings("restriction")
public class ListViewDominoes extends ListView<DominoView> {

    private ObservableList<DominoView> pieces;
    private ArrayList<Dominoes> dominoes;

    private double padding = 20;

    private boolean visibilityHistoric;

    /**
     * This class builder initialize this list and your arrays with values
     * defined in the parameter Array.
     *
     * @param array
     *            Values to initialize this list and your array
     */
    public ListViewDominoes(ArrayList<Dominoes> array) {
        this.visibilityHistoric = true;

        this.pieces = FXCollections.observableList(new ArrayList<DominoView>());
        this.dominoes = new ArrayList<>();

        if (array != null) {
            for (Dominoes dom : array) {
                if (!add(dom))
                    System.out.println("Not added: " + dom);
            }
        }

        this.setItems(this.pieces);

        setOnDragDropped(event -> {
            boolean success = false;
            if (event.getDragboard().hasContent(Dominoes.clipboardFormat)) {
                success = true;
                ObservableList<DominoView> items = getItems();
                Object content = event.getDragboard().getContent(Dominoes.clipboardFormat);
                DominoView selectedItem = ListViewDominoes.this.getSelectionModel().getSelectedItem();
                int toIdx = items.indexOf(selectedItem);
                int fromIdx = dominoes.indexOf(content);
                if (fromIdx >= 0) { // move an item already contained in list
                    moveItems(fromIdx, toIdx - fromIdx);
                    // System.out.println("Moved item from idx " + fromIdx + "
                    // to " + toIdx);
                } else if (content instanceof Dominoes) {
                    add((Dominoes) content);
                    moveItems(dominoes.size() - 1, toIdx - dominoes.size() + 1);
                    // System.out.println("Added new domino: " + content
                    // + " with cardinality " +
                    // ((Matrix2DJava)((Dominoes)content).getMat()).getMatrix().sum());
                } else {
                    System.out.println("Drop failed..");
                    success = false;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
        
//        setOnDragOver(event -> {
//            if (event.getDragboard().hasContent(Dominoes.clipboardFormat)) {
//                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
//            }
//            for (DominoView view : pieces) {
//                System.out.println(view.getBoundsInParent()// + " and " + view.getBoundsInLocal()
//                  + " and " + event.getX() + "," + event.getY()
////                  + " and " + event.getSceneX() + "," + event.getSceneY()
//                  );
//                if (view.getBoundsInParent().contains(event.getX(),event.getY())) {
//                    int index = pieces.indexOf(view);
//                    if (index >= 0) {
//                        ListViewDominoes.this.getSelectionModel().clearAndSelect(index);
//                        break;
//                    }
//                }
//            }
//            event.consume();
//        });
    }

    /**
     * This function adds a Dominoes in the list
     *
     * @param domino
     *            The dominoes to resultMultiplication
     * @return true in affirmative case
     * @throws IllegalArgumentException
     */
    public boolean add(Dominoes domino) throws IllegalArgumentException {

        boolean result = false;

        if (domino == null) {
            return result;
        }
        // modify the equals() method in Dominoes if different behavior is
        // desired
        if (dominoes.contains(domino)) {
            return result;
        }

        DominoView group = domino.drawDominoes();
        group.setVisible(DominoView.Views.GRAPH_HISTORIC, true);

        addMouseHandlers(domino, group);
        // TODO do we really need handlers for every item in the list? Can't we
        // push this up a level?
        addMenuHandlers(group);

        this.dominoes.add(domino);

        this.pieces.add(group);

        result = true;
        return result;
    }

    private void addMouseHandlers(Dominoes domino, DominoView group) {
        Tooltip tooltip = new Tooltip(domino.getMat().getMatrixDescriptor().getRowType() + " x "
                + domino.getMat().getMatrixDescriptor().getColType() + " : "
                + domino.getMat().getMatrixDescriptor().getNumRows() + " x "
                + domino.getMat().getMatrixDescriptor().getNumCols());
        Tooltip.install(group, tooltip);

        group.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if (event.getClickCount() == 2) {
                    System.out.println("copy to area move");
                    copyFromListToAreaMove(group);
                }
            }
        });

        group.setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(Dominoes.clipboardFormat, domino);
            dragboard.setDragView(group.snapshot(null, null));
            dragboard.setDragViewOffsetX(event.getX());
            dragboard.setDragViewOffsetY(event.getY());
            dragboard.setContent(content);
            event.consume();
        });

        group.setOnDragOver(event -> {
            if (event.getGestureSource() != group && event.getDragboard().hasContent(Dominoes.clipboardFormat)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            int index = ListViewDominoes.this.getItems().indexOf((event.getGestureTarget()));
            if (index >= 0) {
                ListViewDominoes.this.getSelectionModel().clearAndSelect(index);
            }
            event.consume();
        });
    }

    private void addMenuHandlers(Group group) {
        ContextMenu minimenu = new ContextMenu();
        MenuItem menuItemToAreaMove = new MenuItem("Copy To Area Move");
        MenuItem menuItemRemove = new MenuItem("Remove");
        minimenu.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                event.consume();
            }
        });
        minimenu.setOnAction(event -> {
            // choose menu item multiply
            String text = ((MenuItem) event.getTarget()).getText();
            if (text.equals(menuItemToAreaMove.getText())) {
                System.out.println("copy to area move");
                copyFromListToAreaMove(group);
            } else if (text.equals(menuItemRemove.getText())) {
                System.out.println("removing");
                try {
                    removeFromListAndArea(group);
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                    ex.printStackTrace();
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

        minimenu.getItems().addAll(menuItemToAreaMove, menuItemRemove);
    }
    
    /**
     * This function is called to change the parts color
     */
    void changeColor() {
        this.pieces.forEach(dominoView -> dominoView.changeColor());
    }

    /**
     * This function remove all parts in this area move
     */
    public void clear() {
        for (int i = 0; i < this.pieces.size(); i++) {
            this.pieces.get(i).setVisible(false);
        }
        this.pieces.removeAll(this.pieces);
        this.dominoes.removeAll(this.dominoes);
    }

    /**
     * This Function copy from this list, to area move, a domino.
     *
     * @param group
     *            domino to copy
     */
    private void copyFromListToAreaMove(Group group) {

        Dominoes auxDomino = this.dominoes.get(this.pieces.indexOf(group));

        // adding in area move
        App.copyToArea(auxDomino.cloneNoMatrix());
    }

    /**
     * This function is used to move a selected domino in the list.
     *
     * @param indexSource
     *            The selected index. The dominoes in this position will suffer
     *            a change in their position
     * @param indexTargetRelative
     *            The position target.
     */
    public void moveItems(int indexSource, int indexTargetRelative) {
        int indexTarget = indexSource + indexTargetRelative;
        // int indexTarget = indexTargetRelative;

        // catch index selected
        if (this.pieces == null || this.dominoes == null) {
            return;
        }

        if ((indexTarget < 0 || indexTarget >= this.pieces.size())
                || (indexSource < 0 || indexSource >= this.pieces.size()) || (indexSource == indexTarget)) {
            return;
        }

        if (indexTarget > indexSource) {
            DominoView sourceGroup = this.pieces.get(indexSource);
            Dominoes sourceDominoes = new Dominoes(Configuration.processingUnit);
            sourceDominoes = this.dominoes.get(indexSource);

            for (int i = indexSource; i < indexTarget; i++) {
                this.pieces.set(i, this.pieces.get(i + 1));
                this.dominoes.set(i, this.dominoes.get(i + 1));
            }

            this.pieces.set(indexTarget, sourceGroup);
            this.dominoes.set(indexTarget, sourceDominoes);

        } else if (indexTarget < indexSource) {
            DominoView sourceGroup = this.pieces.get(indexSource);
            Dominoes sourceDominoes = new Dominoes(Configuration.processingUnit);
            sourceDominoes = this.dominoes.get(indexSource);

            for (int i = indexSource; i > indexTarget; i--) {
                this.pieces.set(i, this.pieces.get(i - 1));
                this.dominoes.set(i, this.dominoes.get(i - 1));
            }

            this.pieces.set(indexTarget, sourceGroup);
            this.dominoes.set(indexTarget, sourceDominoes);
        }
    }

    /**
     * This Functions is used to define the moving area size
     *
     * @param width
     * @param height
     */
    void setSize(double width, double height) {
        this.setMinWidth(width - padding);
        this.setPrefWidth(width);
        this.setMaxWidth(width + padding);
        this.setPrefHeight(height);
    }

    /**
     * This function is used to define the visibility of historic
     *
     * @param visibility
     *            True to define visible the historic
     */
    void setVisibleHistoric() {
        boolean vis = this.visibilityHistoric;
        pieces.forEach(dominoView -> dominoView.setVisible(DominoView.Views.GRAPH_HISTORIC, vis));
    }

    /**
     * This function is used to define the visibility of type
     *
     * @param visibility
     *            True to define visible the type
     */
    void setVisibleType() {
        boolean vis = Configuration.visibilityType;
        pieces.forEach(dominoView -> dominoView.setVisible(DominoView.Views.GRAPH_HISTORIC,vis));
    }

    /**
     * This function remove only a element this list.
     *
     * @param group
     *            element to remove
     * @return true in affimative case
     */
    public boolean remove(Group group) {
        int index = this.pieces.indexOf(group);
        if (index > -1) {
            group.setVisible(false);
            this.dominoes.remove(index);
            this.pieces.remove(index);
            return true;
        }
        return false;
    }

    /**
     * This function remove the element of the list and of the move area
     *
     * @param group
     *            Element to remove
     * @return true, in affirmative case
     * @throws IOException
     */
    private boolean removeFromListAndArea(Group group) throws IOException {
        return App.removeMatrix(this.dominoes.get(pieces.indexOf(group)), group);
    }

}
