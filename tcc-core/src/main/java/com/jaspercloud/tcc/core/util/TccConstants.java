package com.jaspercloud.tcc.core.util;

public interface TccConstants {

    String TID_KEY = "tid";

    enum TccStatus {

        Try("try"), Confirm("confirm"), Cancel("cancel");

        private String value;

        TccStatus(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

        public boolean equals(TccStatus tccStatus) {
            return equals(tccStatus.value);
        }

        public boolean equals(String value) {
            return this.value.equals(value);
        }
    }
}
