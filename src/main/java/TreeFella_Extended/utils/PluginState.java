package TreeFella_Extended.utils;

public class PluginState {
    private boolean isenabled = true;

    public boolean isEnabled() { return isenabled; }
    public void enable() {isenabled = true;}
    public void disable() {isenabled = false;}

    private boolean issneaking = false;

    public boolean isSneaking() { return issneaking; }
    public void enableSneaking() {issneaking = true;}
    public void disableSneaking() {issneaking = false;}
}
