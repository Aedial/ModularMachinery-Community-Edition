package github.kasuminova.mmce.common.upgrade;

import github.kasuminova.mmce.common.event.machine.MachineEvent;

/**
 * A script callback for events emitted by an installed upgrade.
 */
@FunctionalInterface
public interface UpgradeEventHandler {

    void handle(MachineEvent event, MachineUpgrade upgrade);
}
