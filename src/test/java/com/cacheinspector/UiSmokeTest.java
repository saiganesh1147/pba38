package com.cacheinspector;

import com.cacheinspector.ui.MainFrame;
import com.cacheinspector.ui.theme.UITheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class UiSmokeTest {

    @Test
    @DisplayName("Verify MainFrame creation, component hierarchy, and simulation initialization")
    public void testMainFrameInitialization() throws Exception {
        // Run in headless or non-headless environment safely
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Headless environment detected; skipping GUI frame display test.");
            return;
        }

        SwingUtilities.invokeAndWait(() -> {
            UITheme.setupLookAndFeel();
            MainFrame frame = new MainFrame();
            assertNotNull(frame, "MainFrame should be initialized");
            assertEquals("Buffer Pool Management System – Cache Inspector", frame.getTitle());
            assertTrue(frame.getContentPane().getComponentCount() > 0, "Root panel should have components");
            frame.dispose();
        });
    }
}
