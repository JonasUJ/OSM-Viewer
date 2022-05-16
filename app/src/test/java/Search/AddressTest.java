package Search;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AddressTest {

    AddressDatabase addressDatabase;
    Address[] addresses;

    @BeforeEach
    void initiateAddresses() {
        addressDatabase = new AddressDatabase();
        addresses = new Address[11];

        addresses[0] = (new Address("Testvej", "24B", "4050", "Testby", 0, 0));
        addresses[1] = (new Address("Testvej", "6", "4050", "Testby", 0, 0));
        addresses[2] = (new Address("Testvej", "21", "4050", "Testby", 0, 0));
        addresses[3] = (new Address("Testvej", "5", "4080", "Anden Testby", 0, 0));
        addresses[4] = (new Address("Testvej", "27", "4090", "Tredje Testby", 0, 0));
        addresses[5] = (new Address("Anden Testvej", "245", "4050", "Testby", 0, 0));
        addresses[6] = (new Address("Anden Testvej", "3A", "4050", "Testby", 0, 0));
        addresses[7] = (new Address("Anvej", "35", "4080", "Anden Testby", 0, 0));
        addresses[8] = (new Address("Endu En Vej", "21", "5080", "Fjerde Testby", 0, 0));
        addresses[9] = (new Address("Også En Vej", "26", "5080", "Fjerde Testby", 0, 0));
        addresses[10] = (new Address("Også En Vej", "24", "6000", "Fjerde Testby b", 0, 0));

        for (Address a : addresses) {
            addressDatabase.addAddress(a);
        }
        addressDatabase.onFinish();
    }

    // tests

    @Test
    public void testOnlyLetters() {
        // should return addreses which street name starts with input letters,but only one per city

        var list = search("t");
        // there are three valid possibilities since, there are three valid addreses for city "testby",
        // but only one of the should be picked
        int[] shouldContain = {0, 3, 4};
        int[] shouldContain2 = {1, 3, 4};
        int[] shouldContain3 = {2, 3, 4};
        assertTrue(
                containsExactly(list, shouldContain)
                        || containsExactly(list, shouldContain2)
                        || containsExactly(list, shouldContain3));

        var list2 = search(" An");
        int[] shouldContain4 = {5, 7};
        assertTrue(containsExactly(list2, shouldContain4));
    }

    @Test
    public void testOnlyStreets() {
        // should return addresses with the given street name,but only one per city

        var list = search("testvej");
        // there are three valid possibilities since, there are three valid addreses for "testby", but
        // only one of the should be picked
        int[] shouldContain = {0, 3, 4};
        int[] shouldContain2 = {1, 3, 4};
        int[] shouldContain3 = {2, 3, 4};
        assertTrue(
                containsExactly(list, shouldContain)
                        || containsExactly(list, shouldContain2)
                        || containsExactly(list, shouldContain3));

        var list2 = search("Endu En Vej");
        int[] shouldContain4 = {8};
        assertTrue(containsExactly(list2, shouldContain4));
    }

    @Test
    public void testStreetAndHousenumber() {
        // should return addreses which street name starts with input letters, and which housenumber
        // starts with the given number

        var list = search("test 2");
        int[] shouldContain = {0, 2, 4};
        assertTrue(containsExactly(list, shouldContain));

        var list2 = search(" Anden TestVej 3A,");
        int[] shouldContain2 = {6};
        assertTrue(containsExactly(list2, shouldContain2));

        var list3 = search("An 3.");
        int[] shouldContain3 = {6, 7};
        assertTrue(containsExactly(list3, shouldContain3));
    }

    @Test
    public void testStreetAndHousenumberAndCity() {
        // should return addreses which street name starts with the first input letters, and which
        // housenumber starts with the given number, and which city starts with the letters after the
        // number

        var list = search("testvej, 2 test");
        int[] shouldContain = {0, 2};
        assertTrue(containsExactly(list, shouldContain));

        var list2 = search("An 3, anden testby   ");
        int[] shouldContain2 = {7};
        assertTrue(containsExactly(list2, shouldContain2));
    }

    @Test
    public void testStreetAndHousenumberAndPostcode() {
        // should return addresses which street name starts with the first input letters, which
        // housenumber starts with the given number,  and which postcode matches the second number
        // string

        var list = search("tesT 2 . 4090");
        int[] shouldContain = {4};
        assertTrue(containsExactly(list, shouldContain));

        var list2 = search("An,, 3 4050");
        int[] shouldContain2 = {6};
        assertTrue(containsExactly(list2, shouldContain2));

        var list3 = search("tesT 2 . 4050");
        int[] shouldContain3 = {0, 2};
        assertTrue(containsExactly(list3, shouldContain3));
    }

    @Test
    public void testFullAddress() {
        // should return addresses which street name starts with the first input letters, which
        // housenumber starts with the given number, and which city starts with the letters after the
        // number, and which postcode matches the second number string

        var list = search("test  24B   4050 testBy ");
        int[] shouldContain = {0};
        assertTrue(containsExactly(list, shouldContain));

        var list2 = search("Også En Vej, 2 6000 fjerde ");
        int[] shouldContain2 = {10};
        assertTrue(containsExactly(list2, shouldContain2));
    }

    @Test
    public void testFalseInput() {

        // No addresses should be returned if an empty string is given
        var list = search("");
        assertTrue(list == null || list.isEmpty());

        // No addresses should be returned for this input, since it doesn't match any of the street
        // names
        var list2 = search("drgdfgdcfg");
        assertTrue(list2 == null || list2.isEmpty());

        // No addresses should be returned for this input, since the street is not in the given city
        var list3 = search("testvej 2 Fjerde Testby ");
        assertTrue(list3 == null || list3.isEmpty());

        // No addresses should be returned for this input, since the city and the post code doesn't
        // match (correct city)
        var list4 = search("An 3 4050 anden testby ");
        assertTrue(list4 == null || list4.isEmpty());

        // No addresses should be returned for this input, since the street is not in the given postcode
        var list5 = search("testvej 2 6000 ");
        assertTrue(list5 == null || list5.isEmpty());
    }

    // Utils

    private List<Address> search(String input) {

        var address = AddressDatabase.parse(input);
        if (address == null) {
            return null;
        }

        address.street(reformat(address.getStreet()));
        address.city(reformat(address.getCity()));

        return addressDatabase.possibleAddresses(address.build(), 5);
    }

    private boolean containsExactly(List<Address> checkList, int[] numbers) {
        var shouldContain = new ArrayList<Address>();
        for (int i : numbers) {
            shouldContain.add(addresses[i]);
        }

        return containsExactly(checkList, shouldContain);
    }

    private boolean containsExactly(List<Address> checkList, List<Address> input) {
        for (Address a : input) {
            if (!checkList.contains(a)) return false;
        }

        return checkList.size() == input.size();
    }

    public static String reformat(String string) {
        if (string == null) return null;
        string = string.toLowerCase();

        var stringBuilder = new StringBuilder();
        var split = string.split(" ");
        for (String s : split) {
            if (s.length() == 0) continue;
            char[] charArray = s.toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            var result = new String(charArray);
            stringBuilder.append(result).append(" ");
        }

        return stringBuilder.toString().trim();
    }

}
