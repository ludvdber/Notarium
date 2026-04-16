package be.freenote.dto.response;

import java.util.List;

public record DelegateResponse(
        String sectionName,
        String sectionColor,
        List<DelegateMember> members
) {}
