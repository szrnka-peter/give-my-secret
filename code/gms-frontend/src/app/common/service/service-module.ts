import { HttpClientModule } from "@angular/common/http";
import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from "@angular/core";
import { AnnouncementService } from "./announcement-service";
import { ApiTestingService } from "./api-testing-service";
import { ApiKeyService } from "./apikey-service";
import { AuthService } from "./auth-service";
import { EventService } from "./event-service";
import { KeystoreService } from "./keystore-service";
import { MessageService } from "./message-service";
import { SecretService } from "./secret-service";
import { SetupService } from "./setup-service";
import { SharedDataService } from "./shared-data-service";
import { SplashScreenStateService } from "./splash-screen-service";
import { UserService } from "./user-service";
import { SecureStorageService } from "./secure-storage.service";

@NgModule({
    declarations: [ 
      
     ],
    imports: [
        HttpClientModule,
    ],
    providers: [ 
      SetupService, SharedDataService, AuthService, SecretService, EventService, UserService, AnnouncementService, ApiKeyService, MessageService, KeystoreService,
      SplashScreenStateService, ApiTestingService, SecureStorageService
    ],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
  })
  export class ServiceModule { }