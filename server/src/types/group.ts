import { EducationField } from ".";

export interface Group {
    id: number | null;
    name: string;
    course: number | null;
    education_field_id: number | null;
}

export type GroupMapParse = {
    group: Omit<Group, 'education_field_id'>;
    education_field: Omit<EducationField, 'id'>;
}

export type GroupCreate = Pick<Group, 'name'>;