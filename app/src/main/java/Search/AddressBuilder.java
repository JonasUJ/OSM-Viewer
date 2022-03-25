package Search;

import osm.elements.SlimOSMNode;

import java.io.Serializable;

public class AddressBuilder implements Serializable{
    private String street,house,floor,side,postcode,city;
    private SlimOSMNode node;

    public String getStreet(){
        return street;
    }
    public String getPostcode(){
        return postcode;
    }
    public String getCity(){
        return city;
    }
    public String getHouse(){
        return house;
    }

    public void street(String street){
        this.street = street;
    }
    public void house(String house){
        this.house = house;
    }
    public void floor(String floor){
        this.floor = floor;
    }
    public void side(String side){
        this.side = side;
    }
    public void postcode(String postcode){
        this.postcode = postcode;
    }
    public void city(String city){
        this.city = city;
    }
    public void SlimOSMNode(SlimOSMNode node){
        this.node = node;
    }

    public Address build(){
        return new Address(street, house, floor, side, postcode, city, node);
    }

    public String getStringToReplace(Address address){
        StringBuilder stringBuilder = new StringBuilder();

        if(street != null)
            stringBuilder.append(address.street()).append(" ");
        if(house != null)
            stringBuilder.append(address.houseNumber()).append(" ");
        if(floor != null) //TODO: hmmm???
            stringBuilder.append(address.floor()).append(" ");
        if(side != null) //TODO: hmm?
            stringBuilder.append(address.side()).append(" ");
        if(city != null)
            stringBuilder.append(address.city()).append(" ");
        if(postcode != null)
            stringBuilder.append(address.postcode()).append(" ");

        return stringBuilder.toString();
    }
}