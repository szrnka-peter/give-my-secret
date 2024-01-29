import { HttpErrorResponse } from "@angular/common/http";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { ReplaySubject, of, throwError } from "rxjs";
import { AngularMaterialModule } from "../../angular-material-module";
import { AuthenticationPhase, Login, LoginResponse } from "../../common/model/login.model";
import { AuthService } from "../../common/service/auth-service";
import { SharedDataService } from "../../common/service/shared-data-service";
import { SplashScreenStateService } from "../../common/service/splash-screen-service";
import { EMPTY_USER, User } from "../user/model/user.model";
import { LoginComponent } from "./login.component";

/**
 * @author Peter Szrnka
 */
describe('LoginComponent', () => {
    let component : LoginComponent;
    let fixture : ComponentFixture<LoginComponent>;
    let mockSubject : ReplaySubject<any>;
    // Injected services
    let router : any;
    let authService : any;
    let dialog : any = {};
    let sharedDataService : any;
    let splashScreenStateService : any;
    let mockLocation: any;
    let activatedRoute : any = {};

    const configTestBed = () => {
        TestBed.configureTestingModule({
            imports : [ RouterTestingModule, FormsModule, AngularMaterialModule, NoopAnimationsModule ],
            declarations : [LoginComponent],
            providers: [
                { provide : Router, useValue: router },
                { provide : SharedDataService, useValue : sharedDataService },
                { provide : SplashScreenStateService, useValue : splashScreenStateService },
                { provide : AuthService, useValue : authService },
                { provide : MatDialog, useValue : dialog },
                { provide : Location, useValue: mockLocation },
                { provide : ActivatedRoute, useValue : activatedRoute }
            ]
        });

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    };

    beforeEach(() => {
        router = {
            navigate : jest.fn().mockReturnValue(of(true))
        };

        mockSubject = new ReplaySubject<any>();
        sharedDataService = {
            refreshCurrentUserInfo : jest.fn(),
            userSubject$ : mockSubject
        };

        dialog = {
            open : jest.fn()
        };

        splashScreenStateService = {
            start : jest.fn(),
            stop : jest.fn()
        };

        authService = {
            login : jest.fn().mockImplementation(() => {
                return of({
                    currentUser:  {
                        roles: []
                    },
                    phase: AuthenticationPhase.COMPLETED
                });
            })
        };

        mockLocation = {
            path: jest.fn().mockReturnValue("/")
        };

        activatedRoute = {
            snapshot : {
                queryParams: {
                }
            }
        };
    });

    it.each([
        [undefined, []],
        [{
            previousUrl: '/'
        }, ['ROLE_USER']],
        [{
            previousUrl: '/secret/list'
        }, []],
        [{
            previousUrl: '/users/list'
        }, []],
        [{
            previousUrl: '/secret/list'
        }, ['ROLE_ADMIN']],
        [{
            previousUrl: '/users/list'
        }, ['ROLE_ADMIN']],
        [{
            previousUrl: '/secret/list'
        }, ['ROLE_USER']],
        [{
            previousUrl: '/users/list'
        }, ['ROLE_USER']]
    ])('Should create component and login with redirect', (inputQueryParam: any, inputRoles: string[]) => {
        // arrange
        activatedRoute = {
            snapshot : {
                queryParams: inputQueryParam
            }
        };
        authService = {
            login : jest.fn().mockImplementation(() => {
                return of({
                    currentUser:  {
                        roles: inputRoles
                    },
                    phase: AuthenticationPhase.COMPLETED
                });
            })
        };

        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.togglePasswordDisplay();
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(sharedDataService.refreshCurrentUserInfo).toHaveBeenCalled();
        expect(component.showPassword).toBeTruthy();
    });

    it('Should create component and login', () => {
        // arrange
        activatedRoute = {
            snapshot : {
                queryParams: {
                }
            }
        };
        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.togglePasswordDisplay();
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(sharedDataService.refreshCurrentUserInfo).toHaveBeenCalled();
        expect(component.showPassword).toBeTruthy();
    });

    it('Should require MFA with redirect', () => {
        // arrange
        activatedRoute = {
            snapshot : {
                queryParams: {
                    previousUrl: '/users/list'
                }
            }
        };
        const mockResponse: LoginResponse = {
            currentUser: { username: 'test', roles: [] },
            phase: AuthenticationPhase.MFA_REQUIRED
        };
        authService = {
            login : jest.fn().mockImplementation(() => {
                return of(mockResponse);
            })
        };
        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(splashScreenStateService.stop).toHaveBeenCalled();
        expect(router.navigate).toHaveBeenCalledWith(['/verify'], { state: { username: 'test' }, queryParams: { previousUrl: '/users/list' } });
        expect(sharedDataService.refreshCurrentUserInfo).toHaveBeenCalledTimes(0);
    });

    it('Should require MFA with redirect', () => {
        // arrange
        activatedRoute = {
            snapshot : {
                queryParams: {
                    previousUrl: '/users/list'
                }
            }
        };
        const mockResponse: LoginResponse = {
            currentUser: EMPTY_USER,
            phase: AuthenticationPhase.BLOCKED
        };
        authService = {
            login : jest.fn().mockImplementation(() => {
                return of(mockResponse);
            })
        };
        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(splashScreenStateService.stop).toHaveBeenCalled();
    });

    it('Should require MFA', () => {
        // arrange
        const mockResponse: LoginResponse = {
            currentUser: { username: 'test', roles: [] },
            phase: AuthenticationPhase.MFA_REQUIRED
        };
        authService = {
            login : jest.fn().mockImplementation(() => {
                return of(mockResponse);
            })
        };
        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(splashScreenStateService.stop).toHaveBeenCalled();
        expect(router.navigate).toHaveBeenCalledWith(['/verify'], { state: { username: 'test' }, queryParams: { previousUrl: '' } });
        expect(sharedDataService.refreshCurrentUserInfo).toHaveBeenCalledTimes(0);
    });

    it('Should fail after login', () => {
        authService = {
            login : jest.fn().mockReturnValue(throwError(() => new HttpErrorResponse({ error : new Error("OOPS!"), status : 500, statusText: "OOPS!"})))
        };

        configTestBed();
        component.formModel = { username: "user-1", credential : "myPassword1" };

        // act
        component.login();

        // assert
        expect(component).toBeTruthy();
        expect(authService.login).toBeCalledWith({ username: "user-1", credential : "myPassword1" } as Login);
        expect(splashScreenStateService.start).toHaveBeenCalled();
        expect(splashScreenStateService.stop).toHaveBeenCalled();
        expect(sharedDataService.refreshCurrentUserInfo).toBeCalledTimes(0);
    });
});