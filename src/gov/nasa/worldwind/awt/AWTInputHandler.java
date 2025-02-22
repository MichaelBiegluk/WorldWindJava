/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 *
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 *
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.Logging;

import com.jogamp.opengl.awt.GLJPanel;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;

/**
 * @author tag
 * @version $Id: AWTInputHandler.java 2258 2014-08-22 22:08:33Z dcollins $
 */
public class AWTInputHandler extends WWObjectImpl
    implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener, FocusListener, InputHandler,
    Disposable
{
    protected WorldWindow wwd = null;
    protected EventListenerList eventListeners = new EventListenerList();
    protected java.awt.Point mousePoint = new java.awt.Point();
    protected PickedObjectList hoverObjects;
    protected PickedObjectList objectsAtButtonPress;
    protected boolean isHovering = false;
    protected boolean isDragging = false;
    protected boolean forceRedrawOnMousePressed = Configuration.getBooleanValue(AVKey.REDRAW_ON_MOUSE_PRESSED, false);
    protected javax.swing.Timer hoverTimer = new javax.swing.Timer(600, new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            if (AWTInputHandler.this.pickMatches(AWTInputHandler.this.hoverObjects))
            {
                AWTInputHandler.this.isHovering = true;
                AWTInputHandler.this.callSelectListeners(new SelectEvent(AWTInputHandler.this.wwd,
                    SelectEvent.HOVER, mousePoint, AWTInputHandler.this.hoverObjects));
                AWTInputHandler.this.hoverTimer.stop();
            }
        }
    });
    // Delegate handler for View.
    protected SelectListener selectListener;

    public AWTInputHandler()
    {
    }

    @Override
    public void dispose()
    {
        this.hoverTimer.stop();
        this.hoverTimer = null;

        this.setEventSource(null);

        if (this.hoverObjects != null)
            this.hoverObjects.clear();
        this.hoverObjects = null;

        if (this.objectsAtButtonPress != null)
            this.objectsAtButtonPress.clear();
        this.objectsAtButtonPress = null;
    }

    @Override
    public void setEventSource(WorldWindow newWorldWindow)
    {
        if (newWorldWindow != null && !(newWorldWindow instanceof Component))
        {
            String message = Logging.getMessage("Awt.AWTInputHandler.EventSourceNotAComponent");
            Logging.logger().finer(message);
            throw new IllegalArgumentException(message);
        }

        if (newWorldWindow == this.wwd)
        {
            return;
        }

        this.eventListeners = new EventListenerList(); // make orphans of listener references

        if (this.wwd != null)
        {
            Component c = (Component) this.wwd;
            c.removeKeyListener(this);
            c.removeMouseMotionListener(this);
            c.removeMouseListener(this);
            c.removeMouseWheelListener(this);
            c.removeFocusListener(this);

            if (this.selectListener != null)
                this.wwd.removeSelectListener(this.selectListener);

            if (this.wwd.getSceneController() != null)
                this.wwd.getSceneController().removePropertyChangeListener(AVKey.VIEW, this);
        }

        this.wwd = newWorldWindow;
        if (this.wwd == null)
        {
            return;
        }

        this.wwd.getView().getViewInputHandler().setWorldWindow(this.wwd);
        Component c = (java.awt.Component) this.wwd;
        c.addKeyListener(this);
        c.addMouseMotionListener(this);
        c.addMouseListener(this);
        c.addMouseWheelListener(this);
        c.addFocusListener(this);

        this.selectListener = new SelectListener()
        {
            @Override
            public void selected(SelectEvent event)
            {
                if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                {
                    doHover(true);
                }
            }
        };
        this.wwd.addSelectListener(this.selectListener);

        if (this.wwd.getSceneController() != null)
            this.wwd.getSceneController().addPropertyChangeListener(AVKey.VIEW, this);
    }

    public void removeHoverSelectListener()
    {
        hoverTimer.stop();
        hoverTimer = null;
        this.wwd.removeSelectListener(selectListener);
    }

    @Override
    public WorldWindow getEventSource()
    {
        return this.wwd;
    }

    @Override
    public void setHoverDelay(int delay)
    {
        this.hoverTimer.setDelay(delay);
    }

    @Override
    public int getHoverDelay()
    {
        return this.hoverTimer.getDelay();
    }

    public boolean isSmoothViewChanges()
    {
        return this.wwd.getView().getViewInputHandler().isEnableSmoothing();
    }

    public void setSmoothViewChanges(boolean smoothViewChanges)
    {
        this.wwd.getView().getViewInputHandler().setEnableSmoothing(smoothViewChanges);
    }

    public boolean isLockViewHeading()
    {
        return this.wwd.getView().getViewInputHandler().isLockHeading();
    }

    public void setLockViewHeading(boolean lockHeading)
    {
        this.wwd.getView().getViewInputHandler().setLockHeading(lockHeading);
    }

    public boolean isStopViewOnFocusLost()
    {
        return this.wwd.getView().getViewInputHandler().isStopOnFocusLost();
    }

    public void setStopViewOnFocusLost(boolean stopView)
    {
        this.wwd.getView().getViewInputHandler().setStopOnFocusLost(stopView);
    }

    protected WorldWindow getWorldWindow()
    {
        return wwd;
    }

    protected Point getMousePoint()
    {
        return mousePoint;
    }

    protected void setMousePoint(Point mousePoint)
    {
        this.mousePoint = mousePoint;
    }

    protected boolean isHovering()
    {
        return isHovering;
    }

    protected void setHovering(boolean hovering)
    {
        isHovering = hovering;
    }

    protected PickedObjectList getHoverObjects()
    {
        return hoverObjects;
    }

    protected void setHoverObjects(PickedObjectList hoverObjects)
    {
        this.hoverObjects = hoverObjects;
    }

    protected PickedObjectList getObjectsAtButtonPress()
    {
        return objectsAtButtonPress;
    }

    protected void setObjectsAtButtonPress(PickedObjectList objectsAtButtonPress)
    {
        this.objectsAtButtonPress = objectsAtButtonPress;
    }

    @Override
    public boolean isForceRedrawOnMousePressed()
    {
        return forceRedrawOnMousePressed;
    }

    @Override
    public void setForceRedrawOnMousePressed(boolean forceRedrawOnMousePressed)
    {
        this.forceRedrawOnMousePressed = forceRedrawOnMousePressed;
    }

    /*
    public ViewInputHandler getViewInputHandler()
    {
        return viewInputHandler;
    }
     */
    @Override
    public void keyTyped(KeyEvent keyEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (keyEvent == null)
        {
            return;
        }

        this.callKeyTypedListeners(keyEvent);

        if (!keyEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().keyTyped(keyEvent);
        }
    }

    @Override
    public void keyPressed(KeyEvent keyEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (keyEvent == null)
        {
            return;
        }

        this.callKeyPressedListeners(keyEvent);

        if (!keyEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().keyPressed(keyEvent);
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (keyEvent == null)
        {
            return;
        }

        this.callKeyReleasedListeners(keyEvent);

        if (!keyEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().keyReleased(keyEvent);
        }
    }

    /**
     * Construct a mouse event with GL surface screen coordinates
     * @param e
     * @return
     */
    private MouseEvent glMouseEvent(MouseEvent awtMouseEvent)
    {
        int[] GLmousePt = wwd.getSceneController().getDrawContext().awtPointToGLpoint(awtMouseEvent.getPoint());
        MouseEvent e = new MouseEvent(awtMouseEvent.getComponent(), awtMouseEvent.getID(),
            awtMouseEvent.getWhen(), awtMouseEvent.getModifiersEx(),
            GLmousePt[0], GLmousePt[1], awtMouseEvent.getClickCount(), awtMouseEvent.isPopupTrigger(),
            awtMouseEvent.getButton());

        if (awtMouseEvent.isConsumed())		// needed in case this method is overridden by a subclass
            e.consume();

        return e;
    }

    /**
     * Construct a mouse wheel event with GL surface screen coordinates
     * @param e
     * @return
     */
    private MouseWheelEvent glMouseWheelEvent(MouseWheelEvent awtEv)
    {
        int[] GLmousePt = wwd.getSceneController().getDrawContext().awtPointToGLpoint(awtEv.getPoint());
        MouseWheelEvent e = new MouseWheelEvent(awtEv.getComponent(), awtEv.getID(),
            awtEv.getWhen(), awtEv.getModifiersEx(),
            GLmousePt[0], GLmousePt[1], awtEv.getXOnScreen(), awtEv.getYOnScreen(),
            awtEv.getClickCount(), awtEv.isPopupTrigger(),
            awtEv.getScrollType(), awtEv.getScrollAmount(),
            awtEv.getWheelRotation(), awtEv.getPreciseWheelRotation());

        if (awtEv.isConsumed())		// needed in case this method is overridden by a subclass
            e.consume();

        return e;
    }

    @Override
    public void mouseClicked(final MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (this.wwd.getView() == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }
        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);

        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();

        this.callMouseClickedListeners(mouseEvent);

        if (pickedObjects != null && pickedObjects.getTopPickedObject() != null
            && !pickedObjects.getTopPickedObject().isTerrain())
        {
            // Something is under the cursor, so it's deemed "selected".
            if (MouseEvent.BUTTON1 == mouseEvent.getButton())
            {
                if (mouseEvent.getClickCount() <= 1)
                {
                    this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.LEFT_CLICK,
                        mouseEvent, pickedObjects));
                }
                else
                {
                    this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.LEFT_DOUBLE_CLICK,
                        mouseEvent, pickedObjects));
                }
            }
            else if (MouseEvent.BUTTON3 == mouseEvent.getButton())
            {
                this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.RIGHT_CLICK,
                    mouseEvent, pickedObjects));
            }

            this.wwd.getView().firePropertyChange(AVKey.VIEW, null, this.wwd.getView());
        }
        else
        {
            if (!mouseEvent.isConsumed())
            {
                this.wwd.getView().getViewInputHandler().mouseClicked(mouseEvent);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }

        // Determine if the mouse point has changed since the last mouse move event. This can happen if user switches to
        // another window, moves the mouse, and then switches back to the WorldWind window.
        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        boolean mousePointChanged = !mouseEvent.getPoint().equals(this.mousePoint);

        this.mousePoint = mouseEvent.getPoint();
        this.cancelHover();
        this.cancelDrag();

        // If the mouse point has changed then we need to set a new pick point, and redraw the scene because the current
        // picked object list may not reflect the current mouse position.
        if (mousePointChanged && this.wwd.getSceneController() != null)
            this.wwd.getSceneController().setPickPoint(this.mousePoint);

        if (this.isForceRedrawOnMousePressed() || mousePointChanged)
            this.wwd.redrawNow();

        this.objectsAtButtonPress = this.wwd.getObjectsAtCurrentPosition();

        this.callMousePressedListeners(mouseEvent);

        if (this.objectsAtButtonPress != null && objectsAtButtonPress.getTopPickedObject() != null
            && !this.objectsAtButtonPress.getTopPickedObject().isTerrain())
        {
            // Something is under the cursor, so it's deemed "selected".
            if (MouseEvent.BUTTON1 == mouseEvent.getButton())
            {
                this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.LEFT_PRESS,
                    mouseEvent, this.objectsAtButtonPress));
            }
            else if (MouseEvent.BUTTON3 == mouseEvent.getButton())
            {
                this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.RIGHT_PRESS,
                    mouseEvent, this.objectsAtButtonPress));
            }

            // Initiate a repaint.
            this.wwd.getView().firePropertyChange(AVKey.VIEW, null, this.wwd.getView());
        }

        if (!mouseEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().mousePressed(mouseEvent);
        }

        // GLJPanel does not take keyboard focus when the user clicks on it, thereby suppressing key events normally
        // sent to the InputHandler. This workaround calls requestFocus on the GLJPanel each time the user presses the
        // mouse on the GLJPanel, causing GLJPanel to take the focus in the same manner as GLCanvas. Note that focus is
        // passed only when the user clicks the primary mouse button. See
        // http://issues.worldwind.arc.nasa.gov/jira/browse/WWJ-272.
        if (MouseEvent.BUTTON1 == mouseEvent.getButton() && this.wwd instanceof GLJPanel)
        {
            ((GLJPanel) this.wwd).requestFocusInWindow();
        }
    }

    @Override
    public void mouseReleased(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }

        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        this.mousePoint = mouseEvent.getPoint();
        this.callMouseReleasedListeners(mouseEvent);
        if (!mouseEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().mouseReleased(mouseEvent);
        }
        this.doHover(true);
        this.cancelDrag();
    }

    @Override
    public void mouseEntered(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }

        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        this.callMouseEnteredListeners(mouseEvent);
        this.wwd.getView().getViewInputHandler().mouseEntered(mouseEvent);
        this.cancelHover();
        this.cancelDrag();
    }

    @Override
    public void mouseExited(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }

        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        this.callMouseExitedListeners(mouseEvent);
        this.wwd.getView().getViewInputHandler().mouseExited(mouseEvent);

        // Enqueue a redraw to update the current position and selection.
        if (this.wwd.getSceneController() != null)
        {
            this.wwd.getSceneController().setPickPoint(null);
            this.wwd.redraw();
        }

        this.cancelHover();
        this.cancelDrag();
    }

    @Override
    public void mouseDragged(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
            return;

        if (awtMouseEvent == null)
        {
            return;
        }

        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        Point prevMousePoint = this.mousePoint;
        this.mousePoint = mouseEvent.getPoint();
        this.callMouseDraggedListeners(mouseEvent);

        if ((MouseEvent.BUTTON1_DOWN_MASK & mouseEvent.getModifiersEx()) != 0)
        {
            PickedObjectList pickedObjects = this.objectsAtButtonPress;
            if (this.isDragging
                || (pickedObjects != null && pickedObjects.getTopPickedObject() != null
                && !pickedObjects.getTopPickedObject().isTerrain()))
            {
                this.isDragging = true;
                DragSelectEvent selectEvent = new DragSelectEvent(this.wwd, SelectEvent.DRAG, mouseEvent, pickedObjects,
                    prevMousePoint);
                this.callSelectListeners(selectEvent);

                // If no listener consumed the event, then cancel the drag.
                if (!selectEvent.isConsumed())
                    this.cancelDrag();
            }
        }

        if (!this.isDragging)
        {
            if (!mouseEvent.isConsumed())
            {
                this.wwd.getView().getViewInputHandler().mouseDragged(mouseEvent);
            }
        }

        // Redraw to update the current position and selection.
        if (this.wwd.getSceneController() != null)
        {
            this.wwd.getSceneController().setPickPoint(mouseEvent.getPoint());
            this.wwd.redraw();
        }
    }

    @Override
    public void mouseMoved(MouseEvent awtMouseEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseEvent == null)
        {
            return;
        }

        MouseEvent mouseEvent = glMouseEvent(awtMouseEvent);
        this.mousePoint = mouseEvent.getPoint();
        this.callMouseMovedListeners(mouseEvent);

        if (!mouseEvent.isConsumed())
        {
            this.wwd.getView().getViewInputHandler().mouseMoved(mouseEvent);
        }

        // Redraw to update the current position and selection.
        if (this.wwd.getSceneController() != null)
        {
            this.wwd.getSceneController().setPickPoint(mouseEvent.getPoint());
            this.wwd.redraw();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent awtMouseWheelEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (awtMouseWheelEvent == null)
        {
            return;
        }

        MouseWheelEvent mouseWheelEvent = glMouseWheelEvent(awtMouseWheelEvent);
        this.callMouseWheelMovedListeners(mouseWheelEvent);

        if (!mouseWheelEvent.isConsumed())
            this.wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
    }

    @Override
    public void focusGained(FocusEvent focusEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (focusEvent == null)
        {
            return;
        }

        this.wwd.getView().getViewInputHandler().focusGained(focusEvent);
    }

    @Override
    public void focusLost(FocusEvent focusEvent)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (focusEvent == null)
        {
            return;
        }

        this.wwd.getView().getViewInputHandler().focusLost(focusEvent);
    }

    protected boolean isPickListEmpty(PickedObjectList pickList)
    {
        return pickList == null || pickList.size() < 1;
    }

    protected void doHover(boolean reset)
    {
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
        if (!(this.isPickListEmpty(this.hoverObjects) || this.isPickListEmpty(pickedObjects)))
        {
            PickedObject hover = this.hoverObjects.getTopPickedObject();
            PickedObject last = pickedObjects.getTopPickedObject();

            Object oh = hover == null ? null : hover.getObject() != null ? hover.getObject()
                : hover.getParentLayer() != null ? hover.getParentLayer() : null;
            Object ol = last == null ? null : last.getObject() != null ? last.getObject()
                : last.getParentLayer() != null ? last.getParentLayer() : null;
            if (oh != null && ol != null && oh.equals(ol))
            {
                return; // object picked is the hover object. don't do anything but wait for the timer to expire.
            }
        }

        this.cancelHover();

        if (!reset)
        {
            return;
        }

        if ((pickedObjects != null)
            && (pickedObjects.getTopObject() != null)
            && pickedObjects.getTopPickedObject().isTerrain())
        {
            return;
        }

        this.hoverObjects = pickedObjects;
        this.hoverTimer.restart();
    }

    protected void cancelHover()
    {
        if (this.isHovering)
        {
            this.callSelectListeners(new SelectEvent(this.wwd, SelectEvent.HOVER, this.mousePoint, null));
        }

        this.isHovering = false;
        this.hoverObjects = null;
        this.hoverTimer.stop();
    }

    protected boolean pickMatches(PickedObjectList pickedObjects)
    {
        if (this.isPickListEmpty(this.wwd.getObjectsAtCurrentPosition()) || this.isPickListEmpty(pickedObjects))
        {
            return false;
        }

        PickedObject lastTop = this.wwd.getObjectsAtCurrentPosition().getTopPickedObject();

        if (null != lastTop && lastTop.isTerrain())
        {
            return false;
        }

        PickedObject newTop = pickedObjects.getTopPickedObject();
        //noinspection SimplifiableIfStatement
        if (lastTop == null || newTop == null || lastTop.getObject() == null || newTop.getObject() == null)
        {
            return false;
        }

        return lastTop.getObject().equals(newTop.getObject());
    }

    protected void cancelDrag()
    {
        if (this.isDragging)
        {
            this.callSelectListeners(new DragSelectEvent(this.wwd, SelectEvent.DRAG_END, null,
                this.objectsAtButtonPress, this.mousePoint));
        }

        this.isDragging = false;
    }

    @Override
    public void addSelectListener(SelectListener listener)
    {
        this.eventListeners.add(SelectListener.class, listener);
    }

    @Override
    public void removeSelectListener(SelectListener listener)
    {
        this.eventListeners.remove(SelectListener.class, listener);
    }

    protected void callSelectListeners(SelectEvent event)
    {
        for (SelectListener listener : this.eventListeners.getListeners(SelectListener.class))
        {
            listener.selected(event);
        }
    }

    @Override
    public void addKeyListener(KeyListener listener)
    {
        this.eventListeners.add(KeyListener.class, listener);
    }

    @Override
    public void removeKeyListener(KeyListener listener)
    {
        this.eventListeners.remove(KeyListener.class, listener);
    }

    @Override
    public void addMouseListener(MouseListener listener)
    {
        this.eventListeners.add(MouseListener.class, listener);
    }

    @Override
    public void removeMouseListener(MouseListener listener)
    {
        this.eventListeners.remove(MouseListener.class, listener);
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener listener)
    {
        this.eventListeners.add(MouseMotionListener.class, listener);
    }

    @Override
    public void removeMouseMotionListener(MouseMotionListener listener)
    {
        this.eventListeners.remove(MouseMotionListener.class, listener);
    }

    @Override
    public void addMouseWheelListener(MouseWheelListener listener)
    {
        this.eventListeners.add(MouseWheelListener.class, listener);
    }

    @Override
    public void removeMouseWheelListener(MouseWheelListener listener)
    {
        this.eventListeners.remove(MouseWheelListener.class, listener);
    }

    protected void callKeyPressedListeners(KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyPressed(event);
        }
    }

    protected void callKeyReleasedListeners(KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyReleased(event);
        }
    }

    protected void callKeyTypedListeners(KeyEvent event)
    {
        for (KeyListener listener : this.eventListeners.getListeners(KeyListener.class))
        {
            listener.keyTyped(event);
        }
    }

    protected void callMousePressedListeners(MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mousePressed(event);
        }
    }

    protected void callMouseReleasedListeners(MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseReleased(event);
        }
    }

    protected void callMouseClickedListeners(MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseClicked(event);
        }
    }

    protected void callMouseDraggedListeners(MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            listener.mouseDragged(event);
        }
    }

    protected void callMouseMovedListeners(MouseEvent event)
    {
        for (MouseMotionListener listener : this.eventListeners.getListeners(MouseMotionListener.class))
        {
            listener.mouseMoved(event);
        }
    }

    protected void callMouseWheelMovedListeners(MouseWheelEvent event)
    {
        for (MouseWheelListener listener : this.eventListeners.getListeners(MouseWheelListener.class))
        {
            listener.mouseWheelMoved(event);
        }
    }

    protected void callMouseEnteredListeners(MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseEntered(event);
        }
    }

    protected void callMouseExitedListeners(MouseEvent event)
    {
        for (MouseListener listener : this.eventListeners.getListeners(MouseListener.class))
        {
            listener.mouseExited(event);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        if (this.wwd == null)
        {
            return;
        }

        if (this.wwd.getView() == null)
        {
            return;
        }

        if (event == null)
        {
            return;
        }

        if (event.getPropertyName().equals(AVKey.VIEW)
            && (event.getSource() == this.getWorldWindow().getSceneController()))
        {
            this.wwd.getView().getViewInputHandler().setWorldWindow(this.wwd);
        }
    }
}
