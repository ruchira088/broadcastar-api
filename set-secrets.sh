#!/usr/bin/env bash

export $(sed "/^$/d" secrets.env)
