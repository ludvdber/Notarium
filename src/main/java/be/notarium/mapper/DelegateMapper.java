package be.notarium.mapper;

import be.notarium.dto.response.DelegateMember;
import be.notarium.entity.DelegateHistory;
import be.notarium.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DelegateMapper {

    default DelegateMember toMember(DelegateHistory dh) {
        String username = dh.getUser() != null ? dh.getUser().getUsername() : "Inconnu";
        UserProfile profile = dh.getUser() != null ? dh.getUser().getProfile() : null;
        String discord = profile != null ? profile.getDiscord() : null;
        return new DelegateMember(username, discord);
    }
}
