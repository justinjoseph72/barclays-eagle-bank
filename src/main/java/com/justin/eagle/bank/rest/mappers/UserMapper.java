package com.justin.eagle.bank.rest.mappers;

import java.util.Objects;
import java.util.stream.Stream;

import com.justin.eagle.bank.generated.openapi.rest.model.CreateUserRequest;
import com.justin.eagle.bank.generated.openapi.rest.model.CreateUserRequestAddress;
import com.justin.eagle.bank.generated.openapi.rest.model.UserResponse;
import com.justin.eagle.bank.domain.NewUser;
import com.justin.eagle.bank.domain.ProvisionedUser;
import com.justin.eagle.bank.domain.UserAddress;
import com.justin.eagle.bank.domain.UserProfile;
import jakarta.validation.Valid;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    public NewUser createNewUser(CreateUserRequest request) {


        final CreateUserRequestAddress address = request.getAddress();
        return NewUser.builder()
                .profile(UserProfile.builder()
                        .name(request.getName())
                        .emailAddress(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .build())
                .address(UserAddress.builder()
                        .town(address.getTown())
                        .county(address.getCounty())
                        .postCode(address.getPostcode())
                        .addressLines(Stream.of(address.getLine1(),address.getLine2(), address.getLine3())
                                .filter(Objects::nonNull)
                                .toList())
                        .build())
                .build();
    }

    public UserResponse createUserResponse(ProvisionedUser user, @Valid CreateUserRequest createUserRequest) {
        return UserResponse.builder()
                .id(user.externalUserId())
                .createdTimestamp(user.createdTimestamp())
                .address(createUserRequest.getAddress())
                .phoneNumber(createUserRequest.getPhoneNumber())
                .email(createUserRequest.getEmail())
                .name(createUserRequest.getName())
                .updatedTimestamp(user.createdTimestamp())
                .build();
    }

    public UserResponse buildUserResponse(ProvisionedUser user) {
        return UserResponse.builder()
                .id(user.externalUserId())
                .createdTimestamp(user.createdTimestamp())
                .address(buildAddress(user.user().address()))
                .phoneNumber(user.user().profile().phoneNumber())
                .email(user.user().profile().emailAddress())
                .name(user.user().profile().name())
                .updatedTimestamp(user.updatedTimestamp())
                .build();
    }

    private CreateUserRequestAddress buildAddress(UserAddress address) {
        return CreateUserRequestAddress.builder()
                .county(address.county())
                .town(address.town())
                .postcode(address.postCode())
                .line1(address.addressLines().get(0))
                .line2(address.addressLines().get(1))
                .line3(address.addressLines().get(2))
                .build();
    }
}
