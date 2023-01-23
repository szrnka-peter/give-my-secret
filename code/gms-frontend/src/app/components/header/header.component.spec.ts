import { CUSTOM_ELEMENTS_SCHEMA, EventEmitter, NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { NavigationEnd, NavigationStart, Router } from "@angular/router";
import { Observable, of, ReplaySubject } from "rxjs";
import { AngularMaterialModule } from "../../angular-material-module";
import { User } from "../../common/model/user.model";
import { MessageService } from "../../common/service/message-service";
import { SharedDataService } from "../../common/service/shared-data-service";
import { HeaderComponent } from "./header.component";

/**
 * @author Peter Szrnka
 */
class MockServices {
    public url : any;
    public events = new Observable((observer) => {
        observer.next(new NavigationStart(0, 'dummy/url', 'imperative'));
        observer.next(new NavigationEnd(0, 'dummy/url', 'imperative'));
        observer.next(new NavigationEnd(0, '/', 'imperative'));
        return observer;
    });

    navigate(url: string) {
        this.url = url;
    }

    routerLink(commands: any[] | string | null | undefined) {
        console.info(commands);
    }
}

/**
 * @author Peter Szrnka
 */
describe('HeaderComponent', () => {
    let component: HeaderComponent;
    let sharedDataService: any;
    let messageService: any;
    let eventEmitter: EventEmitter<number>;
    let mockSubject: ReplaySubject<any>;
    let currentUser: User | any;

    // Fixtures
    let fixture: ComponentFixture<HeaderComponent>;

    beforeEach(async () => {
        eventEmitter = new EventEmitter<number>();
        mockSubject = new ReplaySubject<any>();

        sharedDataService = {
            logout: jest.fn(),
            messageCountUpdateEvent: eventEmitter,
            userSubject$: mockSubject,
            getAllUnread : jest.fn()
        };

        messageService = {
            getAllUnread: jest.fn().mockReturnValue(of(1))
        };

        TestBed.configureTestingModule({
            imports: [BrowserAnimationsModule, AngularMaterialModule],
            declarations: [HeaderComponent],
            schemas: [CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA],
            providers: [
                { provide: SharedDataService, useValue: sharedDataService },
                { provide: MessageService, useValue: messageService },
                { provide: Router, useClass: MockServices }
            ]
        }).compileComponents();

        currentUser = {
            roles: ["ROLE_ADMIN"],
            userName: "test1",
            userId: 1
        };
    });

    it('should query unread messages', () => {
        fixture = TestBed.createComponent(HeaderComponent);
        component = fixture.componentInstance;
        mockSubject.next(currentUser);
        fixture.detectChanges();
    });

    it('should log out', () => {
        fixture = TestBed.createComponent(HeaderComponent);
        component = fixture.componentInstance;

        component.currentUser = {
            roles: ["ROLE_ADMIN"],
            username: "test1",
            id: 1
        };
        mockSubject.next(currentUser);
        fixture.detectChanges();

        component.logout();

        // assert
        eventEmitter.emit(2);
        expect(component).toBeTruthy();
        expect(sharedDataService.logout).toHaveBeenCalled();
    });
});