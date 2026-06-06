export interface CredentialsNovSU {
    uid: string;
    personId: string;
    lastName: string;
    firstName: string;
    midName: string;
    email: string;
}

export interface CredentialsNovSUFormated {
    uid?: string;
    personId?: string;
    last_name: string;
    first_name: string;
    father_name?: string;
    email?: string;
}