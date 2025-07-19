package cloud.hytora.driver.language;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.common.objects.NetworkEntity;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TranslatedEntry {

    private String translatedString;


    public TranslatedEntry replace(String key, Object value) {
        String toReplace = "{" + key + "}";
        this.translatedString = translatedString.replaceAll(toReplace, value.toString());

        return this;
    }

    public TranslatedEntry applyPlaceHolders(NetworkEntity<?> entity) {
        this.translatedString = entity.replacePlaceHolders(this.translatedString);
        return this;
    }


    public void print() {
        CloudDriver.getInstance().getLogger().info(this.translatedString);
    }

    public void sendTo(CommandSender sender) {
        sender.sendMessage(this.translatedString);
    }
}
