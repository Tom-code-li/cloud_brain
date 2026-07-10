package com.neuCloudBrainMedical.admin.controller.auth;

import com.neuCloudBrainMedical.admin.dto.auth.AdminInfoResponse;
import com.neuCloudBrainMedical.admin.dto.auth.LoginRequest;
import com.neuCloudBrainMedical.admin.dto.auth.LoginResponse;
import com.neuCloudBrainMedical.admin.service.auth.IAuthCommandService;
import com.neuCloudBrainMedical.admin.service.auth.IAuthQueryService;
import com.neuCloudBrainMedical.admin.util.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTests {

	@Test
	void loginShouldDelegateToCommandService() {
		IAuthCommandService commandService = mock(IAuthCommandService.class);
		IAuthQueryService queryService = mock(IAuthQueryService.class);
		AuthController controller = new AuthController(commandService, queryService);
		LoginRequest request = new LoginRequest();
		LoginResponse response = new LoginResponse();
		response.setToken("token");
		when(commandService.login(request)).thenReturn(response);

		Result<LoginResponse> result = controller.login(request);

		assertEquals("token", result.getData().getToken());
		verify(commandService).login(request);
	}

	@Test
	void getAdminInfoShouldDelegateToQueryService() {
		IAuthCommandService commandService = mock(IAuthCommandService.class);
		IAuthQueryService queryService = mock(IAuthQueryService.class);
		AuthController controller = new AuthController(commandService, queryService);
		AdminInfoResponse response = new AdminInfoResponse();
		response.setUsername("admin");
		when(queryService.getAdminInfo("Bearer token")).thenReturn(response);

		Result<AdminInfoResponse> result = controller.getAdminInfo("Bearer token");

		assertEquals("admin", result.getData().getUsername());
		verify(queryService).getAdminInfo("Bearer token");
	}
}
