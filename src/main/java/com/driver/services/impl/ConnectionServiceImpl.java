package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{
        User user = userRepository2.findById(userId).get();

        if(user.getMaskedIP()!=null)
            throw new Exception("Already connected");
        else if (countryName.equalsIgnoreCase(user.getCountry().getCountryName().toString())) {
            return user;
        } else{
            if(user.getServiceProviders()==null){
                throw new Exception("Unable to connect");
            }

                List<ServiceProvider> providers = user.getServiceProviders();
                int min = Integer.MAX_VALUE;
                ServiceProvider serviceProvider1 = null;
                Country country1 = null;

                for(ServiceProvider serviceProvider:providers){
                   List<Country> countryList = serviceProvider.getCountries();

                   for (Country country:countryList){

                       if(countryName.equalsIgnoreCase(country.getCountryName().toString()) && min>serviceProvider.getId()){
                           min=serviceProvider.getId();
                           serviceProvider1=serviceProvider;
                           country1=country;
                       }
                   }
                }
                if(serviceProvider1!=null){
                    Connection connection = new Connection();
                    connection.setUser(user);
                    connection.setServiceProvider(serviceProvider1);

                    String countryCode = country1.getCode();
                    int providerId = serviceProvider1.getId();
                    String masked = countryCode + "." + providerId +"."+ userId;

                    user.setMaskedIP(masked);
                    user.setConnected(true);
                    user.getConnections().add(connection);

                    serviceProvider1.getConnections().add(connection);

                    userRepository2.save(user);
                    serviceProviderRepository2.save(serviceProvider1);

                    return user;
                }
                 else
                     throw new Exception("Unable to connect");
            }
    }
    @Override
    public User disconnect(int userId) throws Exception {
        User user = userRepository2.findById(userId).get();
        if(user.getMaskedIP()==null)
            throw new Exception("Already disconnected");

        user.setMaskedIP(null);
        user.setConnected(false);
        userRepository2.save(user);
        return user;
    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
       User sender = userRepository2.findById(senderId).get();
       User receiver = userRepository2.findById(receiverId).get();

       if(receiver.getConnected()==true){
           String IP = receiver.getMaskedIP();

           String code1 = IP.substring(0,3);

           if(code1.equals(sender.getCountry().getCode())){
               sender.setConnected(true);
               userRepository2.save(sender);
               return sender;
           }else {
               String countryName = "";

               if(code1.equals(CountryName.CHI.toCode()))
                   countryName = CountryName.CHI.toString();
               if(code1.equals(CountryName.JPN.toCode()))
                   countryName = CountryName.JPN.toString();
               if(code1.equals(CountryName.IND.toCode()))
                   countryName = CountryName.IND.toString();
               if(code1.equals(CountryName.USA.toCode()))
                   countryName = CountryName.USA.toString();
               if(code1.equals(CountryName.AUS.toCode()))
                   countryName = CountryName.AUS.toString();

               User updateSender = connect(senderId,countryName);

               if(updateSender.getConnected()==false)
                   throw new Exception("Cannot establish communication");
               else
                   return updateSender;
           }
        }else {
           if(receiver.getCountry().equals(sender.getCountry())){
               sender.setConnected(true);
               userRepository2.save(sender);
               return sender;
           }else
               throw new Exception("Cannot establish communication");
       }
    }
}

