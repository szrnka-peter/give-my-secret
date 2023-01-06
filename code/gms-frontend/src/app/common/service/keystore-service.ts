import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { environment } from "../../../environments/environment";
import { IdNamePair } from "../model/id-name-pair.model";
import { IdNamePairList } from "../model/id-name-pair-list.model";
import { IEntitySaveResponseDto } from "../model/entity-save-response.model";
import { Keystore } from "../model/keystore.model";
import { KeystoreList } from "../model/keystore-list";
import { ServiceBase } from "./service-base";
import { getHeaders } from "../utils/header-utils";

@Injectable()
export class KeystoreService extends ServiceBase<Keystore, KeystoreList> {

    constructor(http : HttpClient) {
        super(http, "keystore");
    }

    public save(keystore : Keystore, file : any) {
        const requestString = JSON.stringify(keystore) as string;

        const formData : FormData = new FormData();
        formData.append("model", requestString);

        if (file !== undefined) {
            formData.append("file", file, file.name);
        }

        return this.http.post<IEntitySaveResponseDto>(environment.baseUrl + "secure/" + this.scope, formData, { withCredentials: true });
    }

    public getAllKeystoreNames() : Observable<IdNamePair[]> {
        return this.http.get<IdNamePairList>(environment.baseUrl + "secure/" + this.scope + '/list_names', { withCredentials: true, headers : getHeaders() }).pipe(map(value => value.resultList));
    }

    public getAllKeystoreAliases(keystoreId : number) : Observable<IdNamePair[]> {
        return this.http.get<IdNamePairList>(environment.baseUrl + "secure/" + this.scope + '/list_aliases/' + keystoreId, { withCredentials: true, headers : getHeaders() }).pipe(map(value => value.resultList));
    }
}