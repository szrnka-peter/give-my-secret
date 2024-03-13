import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { MatDialog } from "@angular/material/dialog";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute, Data, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Observable, ReplaySubject, of } from "rxjs";
import { AngularMaterialModule } from "../../angular-material-module";
import { IEntitySaveResponseDto } from "../../common/model/entity-save-response.model";
import { SharedDataService } from "../../common/service/shared-data-service";
import { SplashScreenStateService } from "../../common/service/splash-screen-service";
import { Event } from "../event/model/event.model";
import { EventService } from "../event/service/event-service";
import { UserData } from "./model/user-data.model";
import { UserService } from "./service/user-service";
import { UserDetailComponent } from "./user-detail.component";
import { MatChipInputEvent } from "@angular/material/chips";

/**
 * @author Peter Szrnka
 */
describe('UserDetailComponent', () => {
    let component : UserDetailComponent;
    let fixture : ComponentFixture<UserDetailComponent>;
    // Injected services
    let router : any;
    let eventServiceMock : any;
    let serviceMock : any;
    let dialog : any = {};
    let sharedDataService : any;
    let activatedRoute : any = {};
    let authModeSubject: ReplaySubject<string>;
    let splashScreenStateService: any = {};

    const configureTestBed = () => {
        TestBed.configureTestingModule({
            imports : [RouterTestingModule, AngularMaterialModule, NoopAnimationsModule, FormsModule ],
            declarations : [UserDetailComponent],
            schemas : [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
            providers: [
                { provide : Router, useValue : router},
                { provide : SharedDataService, useValue : sharedDataService },
                { provide : UserService, useValue : serviceMock },
                { provide : MatDialog, useValue : dialog },
                { provide : ActivatedRoute, useClass : activatedRoute },
                { provide : EventService, useValue : eventServiceMock },
                { provide : SplashScreenStateService, useValue : splashScreenStateService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(UserDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    };

    beforeEach(() => {
        authModeSubject = new ReplaySubject<string>();
        router = {

        };
        sharedDataService = {
            refreshCurrentUserInfo: jest.fn(),
            authModeSubject$: authModeSubject
        };

        dialog = {
            open : jest.fn().mockReturnValue({ afterClosed: jest.fn().mockReturnValue(of()) })
        }
        
        activatedRoute = class {
            data : Data = of({
                entity : {
                    id : 1,
                    name : "Test User",
                    roles : [ "ROLE_USER" ],
                    email : "test.email@mail.com",
                    status : "ACTIVE",
                    username : "user.name"
                } as UserData
            })
        };

        eventServiceMock = {
            listByUserId : jest.fn().mockReturnValue(of([ { id: 1, username: 'user1', eventDate: new Date(), operation: 'SAVE', target: 'KEYSTORE' } as Event ]))
        };

        serviceMock = {
            save : jest.fn().mockReturnValue(of({ entityId : 1, success : true }) as Observable<IEntitySaveResponseDto>)
        };

        splashScreenStateService = {
            start: jest.fn(),
            stop: jest.fn()
        };
    });

    it('should save new entity', () => {
        // arrange
        const chipInputMock : any = {
            clear : jest.fn()
        };

        const matAutocompleteSelectedEvent : any = {
            option : {
                viewValue : "ROLE_ADMIN"
            }
        };

        eventServiceMock = {
            listByUserId : jest.fn().mockReturnValue(of(undefined))
        };

        activatedRoute = class {
            data : Data = of({
                entity : {
                    name : "Test User",
                    roles : [ "ROLE_USER" ],
                    email : "test.email@mail.com",
                    status : "ACTIVE",
                    username : "user.name"
                } as UserData
            })
        };

        configureTestBed();

        // act
        authModeSubject.next("db");
        component.add({ value : "ROLE_VIEWER", chipInput : chipInputMock } as MatChipInputEvent);
        component.remove("ROLE_VIEWER");
        component.remove("FAKE_ROLE");
        
        component.selected(matAutocompleteSelectedEvent);
        component.add({ value : "ROLE_ADMIN", chipInput : chipInputMock } as MatChipInputEvent);
        component.selected(matAutocompleteSelectedEvent);
        component.remove("ROLE_ADMIN");
        component.remove("FAKE_ROLE");

        component.add({ value : ' ', chipInput : chipInputMock } as MatChipInputEvent);

        component.save();

        // assert
        expect(component).toBeTruthy();
        expect(component.getCount()).toEqual(0);
    });

    it('should save details', () => {
        // arrange
        const chipInputMock : any = {
            clear : jest.fn()
        };

        const matAutocompleteSelectedEvent : any = {
            option : {
                viewValue : "ROLE_ADMIN"
            }
        };

        configureTestBed();

        // act
        authModeSubject.next("db");
        component.add({ value : "ROLE_VIEWER", chipInput : chipInputMock } as MatChipInputEvent);
        component.remove("ROLE_VIEWER");
        component.remove("FAKE_ROLE");
        
        component.selected(matAutocompleteSelectedEvent);
        component.add({ value : "ROLE_ADMIN", chipInput : chipInputMock } as MatChipInputEvent);
        component.selected(matAutocompleteSelectedEvent);
        component.remove("ROLE_ADMIN");
        component.remove("FAKE_ROLE");

        component.add({ value : ' ', chipInput : chipInputMock } as MatChipInputEvent);

        component.save();

        // assert
        expect(component).toBeTruthy();
        expect(component.getCount()).toEqual(1);
    });
});