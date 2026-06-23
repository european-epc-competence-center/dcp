package de.eecc.dcp.api;

import lombok.Getter;

/**
 * Main facade for DCP verifier-side presentation flows.
 *
 * <p>Implementation will be added in subsequent phases; this stub allows module wiring and Spring auto-configuration.
 */
@Getter
public final class DcpPresentation {

    private final DcpOptions options;

    private DcpPresentation(DcpOptions options) {
        this.options = options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DcpPresentation create(DcpOptions options) {
        return builder().options(options).build();
    }

    public static final class Builder {

        private DcpOptions options = DcpOptions.builder().build();

        public Builder options(DcpOptions options) {
            this.options = options;
            return this;
        }

        public DcpPresentation build() {
            return new DcpPresentation(options);
        }
    }
}
