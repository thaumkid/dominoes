package domain;

import java.io.IOException;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class DominoView extends Group {
    public final static double GRAPH_WIDTH = 100;
    public final static double GRAPH_HEIGHT = 50;

    private final static Color COLOR_BACK = new Color(1, 1, 1, 1);
    private final static Color COLOR_BORDER = new Color(0.56, 0.56, 0.56, 1);
    private final static Color COLOR_LINE = new Color(0.56, 0.76, 0.56, 1);
    private final static Color COLOR_BORDER_DERIVED = new Color(0.36, 0.36, 0.36, 1);
    private final static Color COLOR_NORMAL_FONT = new Color(0, 0, 0, 1);
    private final static Color COLOR_NO_OPERATION_FONT = new Color(1, 0, 0, 1);
    private final static Color COLOR_OPERATE_FONT = new Color(0, 1, 0, 1);
    private final static Color COLOR_HISTORIC = new Color(0.86, 0.86, 0.86, 1);
    private final static Color COLOR_INVISIBLE = new Color(0, 0, 0, 0);
    private final static Color COLOR_TYPE = COLOR_BORDER;

    /*
     * This variables are used to know the sequence of the elements, the Group
     * (Graphically) relative to this Domino, in time of the insert
     */
    public final static int GRAPH_ID_ROW = 5;
    public final static int GRAPH_ID_COL = 6;
    private final static int GRAPH_NORMAL_FONT_SIZE = 15;
    public final static int GRAPH_AGGREG_FONT_SIZE = 12;

    final static double GRAPH_ARC = 10;

    public static enum Views {
        GRAPH_HISTORIC,
        GRAPH_TYPE
    }

    private final Rectangle border, line, back;
    private final Text idRow, idCol, historic;
    private final Group graphType;

    public DominoView(Dominoes dom) {
        border = new Rectangle(0, 0, GRAPH_WIDTH, GRAPH_HEIGHT);
        border.setFill(COLOR_BORDER);
        border.setArcHeight(GRAPH_ARC);
        border.setArcWidth(GRAPH_ARC);

        back = new Rectangle(1, 1, GRAPH_WIDTH - 2, GRAPH_HEIGHT - 2);
        back.setFill(COLOR_BACK);
        back.setArcHeight(GRAPH_ARC);
        back.setArcWidth(GRAPH_ARC);

        line = new Rectangle(GRAPH_WIDTH / 2 - 1, border.getHeight() - back.getHeight(), 2, back.getHeight() - 2);
        line.setFill(COLOR_LINE);
        line.setArcHeight(GRAPH_ARC);
        line.setArcWidth(GRAPH_ARC);

        idRow = new Text(dom.getIdRow());
        idRow.setFill(COLOR_NORMAL_FONT);
        idRow.setX(5);
        idRow.setY(2 * GRAPH_HEIGHT / 5);
        idRow.toFront();
        if (dom.getIdRow().startsWith(Dominoes.AGGREG_TEXT))
            idRow.setFont(new Font("Arial", GRAPH_AGGREG_FONT_SIZE));
        else
            idRow.setFont(new Font("Arial", GRAPH_NORMAL_FONT_SIZE));

        idCol = new Text(dom.getIdCol());
        idCol.setFill(COLOR_NORMAL_FONT);
        idCol.setX(GRAPH_WIDTH / 2 + 5);
        idCol.setY(2 * GRAPH_HEIGHT / 5);
        idCol.toFront();
        if (dom.getIdCol().startsWith(Dominoes.AGGREG_TEXT))
            idCol.setFont(new Font("Arial", GRAPH_AGGREG_FONT_SIZE));
        else
            idCol.setFont(new Font("Arial", GRAPH_NORMAL_FONT_SIZE));

        String auxHistoric = dom.getHistoric().toString();
        if (auxHistoric.length() <= 24) {
            historic = new Text(auxHistoric);
        } else {
            historic = new Text(auxHistoric.substring(0, 24) + "...");
        }

        historic.setFont(new Font("Arial", 10));
        historic.setFill(COLOR_HISTORIC);
        historic.setX(2);
        historic.setY(3 * GRAPH_HEIGHT / 5);
        historic.setWrappingWidth(GRAPH_WIDTH - 2);
        historic.toFront();

        // Circle circle = new Circle(back.getX() + back.getWidth() / 2,
        // back.getY() + back.getHeight() / 2, 5, Dominoes.COLOR_TYPE);
        double radius = 5;
        double circlePadding = 2;
        double padding = 1;
        Circle circle = new Circle(0, 0, radius, COLOR_TYPE);

        Text type = new Text();
        type.setFill(COLOR_NORMAL_FONT);
        type.setX(circle.getCenterX() - circle.getRadius() / 2 - padding);
        type.setY(circle.getCenterY() + circle.getRadius() / 2 + padding);

        type.setText(dom.getType().getCode());
        switch (dom.getType()) {
        case BASIC:
            type.setFill(COLOR_INVISIBLE);

            circle.setFill(COLOR_INVISIBLE);

            historic.setFill(COLOR_INVISIBLE);

            break;
        case DERIVED:
            type.setFill(COLOR_INVISIBLE);

            circle.setFill(COLOR_INVISIBLE);

            border.setFill(COLOR_BORDER_DERIVED);
            back.setWidth(back.getWidth() - 2);
            back.setHeight(back.getHeight() - 2);
            back.setX(back.getX() + 1);
            back.setY(back.getY() + 1);

            line.setFill(COLOR_LINE);

            break;
        case SUPPORT:
        case CONFIDENCE:
        case LIFT:
            break;
        }

        circle.toFront();
        type.toFront();

        graphType = new Group(circle, type);
        graphType.setTranslateX(border.getX() + border.getWidth() - (radius + circlePadding));
        graphType.setTranslateY((radius + circlePadding));
        graphType.setAutoSizeChildren(true);

        this.getChildren().addAll(border, back, line, historic, graphType, idRow, idCol);
        Tooltip.install(this, new Tooltip(dom.getIdRow() + "x" + dom.getIdCol()));
    }

    public void setVisible(Views view, boolean vis) {
        switch (view) {
        case GRAPH_HISTORIC:
            historic.setVisible(vis);
            break;
        case GRAPH_TYPE:
            graphType.setVisible(vis);
            break;
        }
    }
    
    public void colorOperateRow() {
        idRow.setFill(COLOR_OPERATE_FONT);
    }
    
    public void colorOperateCol() {
        idCol.setFill(COLOR_OPERATE_FONT);
    }
    
    public void colorNoOperateRow() {
        idRow.setFill(COLOR_NO_OPERATION_FONT);
    }
    
    public void colorNoOperateCol() {
        idCol.setFill(COLOR_NO_OPERATION_FONT);
    }
    
    public void colorNormalRow() {
        idRow.setFill(COLOR_NORMAL_FONT);
    }
    
    public void colorNormalCol() {
        idCol.setFill(COLOR_NORMAL_FONT);
    }

    public void changeColor() {
        border.setFill(COLOR_BORDER);
        back.setFill(COLOR_BACK);
        line.setFill(COLOR_BORDER);
        idRow.setFill(COLOR_NORMAL_FONT);
        idCol.setFill(COLOR_NORMAL_FONT);
    }
    
    /**
     * This function makes a simple animation to tranpose a matrix
     * @param transposing 
     *
     * @param piece The piece to animate
     */
    public void animateTranspose(DominoView other, BooleanProperty transposing) throws IOException {
        transposing.set(true);
        int duration = 500;
        
        double startAngle = getRotate();
        
        double swapFontSize = idCol.getFont().getSize();
        idCol.setFont(idRow.getFont());
        idRow.setFont(new Font(swapFontSize));
        
        double translateX = idRow.getX();
        idRow.setX(idCol.getX());
        idCol.setX(translateX);       
        
        idRow.setText(other.idRow.getText());
        idCol.setText(other.idCol.getText());
        
        RotateTransition rtPiece = new RotateTransition(Duration.millis(duration));
        rtPiece.setFromAngle(startAngle);
        rtPiece.setToAngle(startAngle + 180);

        RotateTransition rtPieceRow = new RotateTransition(Duration.millis(duration));
        rtPieceRow.setFromAngle(rtPiece.getFromAngle());
        rtPieceRow.setToAngle(startAngle - 180);

        RotateTransition rtPieceCol = new RotateTransition(Duration.millis(duration));
        rtPieceCol.setFromAngle(rtPiece.getFromAngle());
        rtPieceCol.setToAngle(rtPieceRow.getToAngle());

        RotateTransition rtType = new RotateTransition(Duration.millis(duration));
        rtType.setFromAngle(rtPiece.getFromAngle());
        rtType.setToAngle(rtPiece.getToAngle());
        
        Color colorHistoric = (Color)historic.getFill();
        FillTransition ftHistoric1 = new FillTransition(Duration.millis(duration));
        ftHistoric1.setFromValue(colorHistoric);
        ftHistoric1.setToValue(COLOR_INVISIBLE);
        
        FillTransition ftHistoric2 = new FillTransition(Duration.millis(duration));
        ftHistoric2.setFromValue(ftHistoric1.getToValue());
        ftHistoric2.setToValue(ftHistoric1.getFromValue());
        
        Color colorType = (Color) ((Shape) graphType.getChildren().get(0)).getFill();
        FillTransition ftType1 = new FillTransition(Duration.millis(duration));
        ftType1.setFromValue(colorType);
        ftType1.setToValue(COLOR_INVISIBLE);
        
        Color colorFontType = (Color) ((Text)graphType.getChildren().get(1)).getFill();
        FillTransition ftType2 = new FillTransition(Duration.millis(duration));
        ftType2.setFromValue(colorFontType);
        ftType2.setToValue(COLOR_INVISIBLE);
        
        FillTransition ftType3 = new FillTransition(Duration.millis(duration));
        ftType3.setFromValue(ftType1.getToValue());
        ftType3.setToValue(ftType1.getFromValue());
        
        FillTransition ftType4 = new FillTransition(Duration.millis(duration));
        ftType4.setFromValue(ftType2.getToValue());
        ftType4.setToValue(ftType2.getFromValue());

        ParallelTransition transition1_1 = new ParallelTransition(new SequentialTransition(graphType.getChildren().get(0), ftType1));
        ParallelTransition transition1_2 = new ParallelTransition(new SequentialTransition(graphType.getChildren().get(1), ftType2));
        ParallelTransition transition1_3 = new ParallelTransition(historic, ftHistoric1);
        
        transition1_1.play();
        transition1_2.play();
        transition1_3.play();
        
        ParallelTransition transition2_1 = new ParallelTransition(this, rtPiece);
        ParallelTransition transition2_2 = new ParallelTransition(idRow, rtPieceRow);
        ParallelTransition transition2_3 = new ParallelTransition(idCol, rtPieceCol);
        
        if(!colorFontType.equals(COLOR_INVISIBLE)
                || !colorHistoric.equals(COLOR_INVISIBLE)){
            transition2_1.setDelay(Duration.millis(duration));
            transition2_2.setDelay(Duration.millis(duration));
            transition2_3.setDelay(Duration.millis(duration));
        }
        
        transition2_1.play();
        transition2_2.play();
        transition2_3.play();
        
        ParallelTransition transition3_1 = new ParallelTransition(historic, ftHistoric2);
        ParallelTransition transition3_2 = new ParallelTransition(graphType.getChildren().get(0), ftType3);
        ParallelTransition transition3_3 = new ParallelTransition(graphType.getChildren().get(1), ftType4);
        
        if(!colorFontType.equals(COLOR_INVISIBLE)
                || !colorHistoric.equals(COLOR_INVISIBLE)){
            transition3_1.setDelay(Duration.millis(2 * duration));
            transition3_2.setDelay(Duration.millis(2 * duration));
            transition3_3.setDelay(Duration.millis(2 * duration));
        }
        
        transition3_1.play();
        transition3_2.play();
        transition3_3.play();
        
        transition1_1.setOnFinished(event -> {
            double x = graphType.getTranslateX();
            double y = graphType.getTranslateY();
            x = Math.abs(GRAPH_WIDTH - x);
            y = Math.abs(GRAPH_HEIGHT - y);
            graphType.setTranslateX(x);
            graphType.setTranslateY(y);
            
            historic.setRotate(startAngle - 180);
            graphType.setRotate(startAngle - 180);
            
            historic.setText(other.historic.getText());
            ((Text) graphType.getChildren().get(1)).setText(((Text)other.graphType.getChildren().get(1)).getText());
            
        });
        
        transition3_3.setOnFinished(event -> transposing.set(false));

    }
    
    public void setRowText(String text, Font font) {
       idRow.setText(text);
       idRow.setFont(font);
    }
    
    public void setColText(String text, Font font) {
        idCol.setText(text);
        idCol.setFont(font);
    }
}