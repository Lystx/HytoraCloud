package cloud.hytora.driver.commands.exceptions;

import cloud.hytora.common.misc.StringUtils;
import io.netty.util.internal.StringUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * If an argument used for executing a command is invalid<br>
 * By calling this exception you can pass on objects to be inserted
 * into the error message string of the {@link Type}
 *
 * @see Type
 */
@Getter
public class CommandException extends RuntimeException {

    private Type type;
    private List<Object> replacements;

    public CommandException(Type type, Object... replacements) {
        this.type = type;
        this.replacements = Arrays.asList(replacements);
    }

    public enum Type {

        CUSTOM("{0}"),
        SIMPLE("Invalid argument ({0})!"),
        CONVERT("Invalid argument ({0})! Couldn't convert to {1}!"),
        VALIDATE("Invalid argument ({0})! It has to be {1}!");

        private String message;

        Type(String msg) {
            this.message = msg;
        }

        public String getMessage(List<Object> replacements) {
            if(replacements == null) replacements = new ArrayList<>();
            return StringUtils.formatCustom(message, replacements.toArray());
        }

    }

}
