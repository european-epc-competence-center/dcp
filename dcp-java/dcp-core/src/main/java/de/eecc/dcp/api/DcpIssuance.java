package de.eecc.dcp.api;

import lombok.Getter;

/**
 * Main facade for DCP issuer-side credential offer flows.
 *
 * <p>Implementation will be added in subsequent phases; this stub allows module wiring and Spring auto-configuration.
 */
@Getter
public final class DcpIssuance {

    private final DcpOptions options;

    private DcpIssuance(DcpOptions options) {
        this.options = options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static DcpIssuance create(DcpOptions options) {
        return builder().options(options).build();
    }

    public static final class Builder {

        private DcpOptions options = DcpOptions.builder().build();

        public Builder options(DcpOptions options) {
            this.options = options;
            return this;
        }

        public DcpIssuance build() {
            return new DcpIssuance(options);
        }
    }
}
