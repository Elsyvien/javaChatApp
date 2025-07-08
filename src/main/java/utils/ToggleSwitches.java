package utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/*
 * Since there are not native swing UI toggle switches,
 * We have to implement our own toggle switches.
 * This class will handle the toggle switch functionality.
 * It will allow toggling between two states (on/off) and can be used in various
 * parts of the application where a toggle switch is needed.
 * @author Max Staneker, Mia Schienagel
 * @version 0.1.0
 */
public class ToggleSwitches extends JComponent {
    
    private boolean isOn = false;
    private Color colorOff = new Color(200, 200, 200);
    private Color colorOn = new Color(0, 150, 255);
    private Color thumbColor = Color.WHITE;
    private int thumbSize = 20;
    private int switchWidth = 50;
    private int switchHeight = 25;
    private List<ToggleListener> listeners = new ArrayList<>();
    
    public interface ToggleListener {
        void onToggle(boolean isOn);
    }
    
    public ToggleSwitches() {
        setPreferredSize(new Dimension(switchWidth, switchHeight));
        setOpaque(false);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        });
    }
    
    public ToggleSwitches(boolean initialState) {
        this();
        this.isOn = initialState;
    }
    
    public void toggle() {
        isOn = !isOn;
        repaint();
        notifyListeners();
    }
    
    public void setOn(boolean on) {
        if (this.isOn != on) {
            this.isOn = on;
            repaint();
            notifyListeners();
        }
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public void addToggleListener(ToggleListener listener) {
        listeners.add(listener);
    }
    
    public void removeToggleListener(ToggleListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (ToggleListener listener : listeners) {
            listener.onToggle(isOn);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background track
        Color backgroundColor = isOn ? colorOn : colorOff;
        g2d.setColor(backgroundColor);
        
        RoundRectangle2D track = new RoundRectangle2D.Float(0, 0, switchWidth, switchHeight, 
                                                           switchHeight, switchHeight);
        g2d.fill(track);
        
        // Draw thumb
        g2d.setColor(thumbColor);
        int thumbX = isOn ? switchWidth - thumbSize - 2 : 2;
        int thumbY = (switchHeight - thumbSize) / 2;
        
        g2d.fillOval(thumbX, thumbY, thumbSize, thumbSize);
        
        // Add subtle shadow to thumb
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillOval(thumbX + 1, thumbY + 1, thumbSize, thumbSize);
        
        g2d.dispose();
    }
    
    // Setter methods for customization
    public void setColorOff(Color colorOff) {
        this.colorOff = colorOff;
        repaint();
    }
    
    public void setColorOn(Color colorOn) {
        this.colorOn = colorOn;
        repaint();
    }
    
    public void setThumbColor(Color thumbColor) {
        this.thumbColor = thumbColor;
        repaint();
    }
    
    public void setSwitchSize(int width, int height) {
        this.switchWidth = width;
        this.switchHeight = height;
        this.thumbSize = height - 4;
        setPreferredSize(new Dimension(width, height));
        repaint();
    }
}
