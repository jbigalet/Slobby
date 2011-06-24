package tvshows_renamer;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

public class Directory_Check_Launcher extends SingleFrameApplication {

    @Override protected void startup(){
        try{
            show(new Directory_Check());
        } catch (Exception e) {}
    }

    @Override protected void configureWindow(java.awt.Window root) {
    }

    public static Directory_Check_Launcher getApplication() {
        return Application.getInstance(Directory_Check_Launcher.class);
    }

    public static void Launch() {
        launch(Directory_Check_Launcher.class, null);
    }
}
