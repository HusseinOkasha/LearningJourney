package com.example.project6.Service;

import com.example.project6.dao.DBItemRepository;
import com.example.project6.entity.DBItem;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    final private DBItemRepository dbItemRepository;

    public AccountService(DBItemRepository dbItemRepository) {
        this.dbItemRepository = dbItemRepository;
    }

    public DBItem save(DBItem item){
        // generate uuid for the account if it doesn't have one.
        if(item.getAccountUuid() == null){
            item.setAccountUuid(UUID.randomUUID());
        }
        
        // sets the primary key to "ACCOUNT#account_uuid".
        item.setPk(String.format("ACCOUNT#%s", item.getAccountUuid()));

        // sets the sort key (range key) to "Account#account_uuid".
        item.setSk(String.format("ACCOUNT#%s", item.getAccountUuid()));
        
        return  dbItemRepository.save(item);
    }


}
