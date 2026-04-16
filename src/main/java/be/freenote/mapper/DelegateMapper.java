package be.freenote.mapper;

import be.freenote.dto.response.DelegateHistoryResponse;
import be.freenote.dto.response.DelegateMember;
import be.freenote.entity.DelegateHistory;
import be.freenote.entity.UserProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DelegateMapper {

    default DelegateMember toMember(DelegateHistory dh) {
        String username = dh.getUser() != null ? dh.getUser().getUsername() : "Inconnu";
        UserProfile profile = dh.getUser() != null ? dh.getUser().getProfile() : null;
        String discord = profile != null ? profile.getDiscord() : null;
        String displayName = profile != null ? profile.getDisplayName() : null;
        Long userId = dh.getUser() != null ? dh.getUser().getId() : null;
        return new DelegateMember(
                dh.getId(),
                userId,
                displayName,
                username,
                discord,
                dh.getStartDate(),
                dh.getEndDate()
        );
    }

    default DelegateHistoryResponse toHistoryResponse(DelegateHistory dh) {
        return new DelegateHistoryResponse(
                dh.getId(),
                dh.getSection().getName(),
                dh.getStartDate(),
                dh.getEndDate(),
                dh.getEndDate() == null
        );
    }
}
