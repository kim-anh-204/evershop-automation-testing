package com.evershop.testdata;

/**
 * Simple POJO helper for test data used by AddAddressTest.
 * Add / adjust values as needed for your environment.
 */
public class AddressData {
    private String fullName;
    private String telephone;
    private String address;
    private String city;
    private String country;
    private String province;
    private String postcode;

    public AddressData(String fullName, String telephone, String address,
                       String city, String country, String province, String postcode) {
        this.fullName = fullName;
        this.telephone = telephone;
        this.address = address;
        this.city = city;
        this.country = country;
        this.province = province;
        this.postcode = postcode;
    }

    // --- Getters ---
    public String getFullName() { return fullName; }
    public String getTelephone() { return telephone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getProvince() { return province; }
    public String getPostcode() { return postcode; }

    @Override
    public String toString() {
        return "AddressData{" +
                "fullName='" + fullName + '\'' +
                ", telephone='" + telephone + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", province='" + province + '\'' +
                ", postcode='" + postcode + '\'' +
                '}';
    }

    // --- Factory methods used in tests ---

    /** Valid US address used by positive test */
    public static AddressData getValidUSAddress() {
        return new AddressData(
                "Nguyen Van A",
                "0988956088",
                "1600 Pennsylvania Ave NW",
                "Washington",
                "United States",
                "Washington",
                "20500"
        );
    }

    /** Generic reasonable address data for negative tests */
    public static AddressData getSomeAddress() {
        return new AddressData(
                "Ming",
                "0123211412",
                "88 Nanjing Road",
                "Shanghai",
                "China",
                "Shanghai",
                "200000"
        );
    }

    /** Full name longer than 50 chars (56 chars example) */
    public static AddressData getAddressWithLongFullName() {
        return new AddressData(
                "Nguyen Thi Kim Mai Hoang Phuong Thao Tran Minh Chau",
                "0345122099",
                "12 Rue Didouche Mourad",
                "Algiers",
                "Algeria",
                "Constantine",
                "16000"
        );
    }

    /** Telephone shorter than 8 digits */
    public static AddressData getAddressWithShortTelephone() {
        return new AddressData(
                "Anna",
                "1234567", // 7 digits
                "12 Rue Didouche Mourad",
                "Alger",
                "Algeria",
                "Alger",
                "16000"
        );
    }

    /** Telephone longer than 11 digits */
    public static AddressData getAddressWithLongTelephone() {
        return new AddressData(
                "Anna Han",
                "012343211234", // 12+ digits
                "45 Gangnam-daero",
                "Seoul",
                "South Korea",
                "Seoul-teukbyeolsi",
                "6030"
        );
    }

    /** Very long address > 100 chars (example) */
    public static AddressData getAddressWithVeryLongAddress() {
        String longAddr = "12 Rue Didouche Mourad, Bab El Oued, Wilaya d'Alger, Apartment 101, Building B, " +
                "Near Central Market, Landmark: Notre-Dame d'Afrique, Additional info: Floor 5, Door 12, Extra details to exceed 100 chars";
        return new AddressData(
                "An",
                "0345122088",
                longAddr,
                "Algiers",
                "Algeria",
                "Alger",
                "16000"
        );
    }

    /** City longer than 50 chars */
    public static AddressData getAddressWithLongCity() {
        return new AddressData(
                "An",
                "0345122088",
                "24 MG Road",
                "A Very Long City Name That Exceeds Fifty Characters For Testing Purposes",
                "India",
                "Karnataka",
                "560001"
        );
    }

    /** Postcode longer than 20 chars */
    public static AddressData getAddressWithLongPostcode() {
        return new AddressData(
                "An",
                "0345122088",
                "29 MG Road",
                "Bengaluru",
                "India",
                "Karnataka",
                "560001-EXTRA-POSTCODE-TO-EXCEED-LIMIT-12345"
        );
    }
}
